package ru.gnusinay.server;

import ru.gnusinay.client.CommandType;
import ru.gnusinay.protocol.Packet;
import ru.gnusinay.protocol.Protocol;
import ru.gnusinay.vfs.VirtualFileSystem;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Класс описывающий работу сервер-приложения. Класс
 * в потоке опрашивает открытые каналы, устанавливает
 * соединения с клиентами, формирует задачи на чтение/запись
 * и добавляет их в очередь на выполнение. Содержит
 * список активных пользователей и их количество.
 * */
public class Server {
    private Selector selector;
    private CommandDriver driver;
    private AtomicInteger countActiveUsers = new AtomicInteger(0);
    private BlockingQueue<ChannelTask> channelTasks = new ArrayBlockingQueue<ChannelTask>(Config.TASK_QUEUE_SIZE);
    private ConcurrentSkipListSet<User> activeUsers = new ConcurrentSkipListSet<>(new UserComparator());

    /** Конструктор принимает объект типа CommandDriver, который реализует
     * логику обработки пользовательских команд
     * */
    Server(CommandDriver driver) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(Config.SERVER_ADDRESS, Config.SERVER_PORT));
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.driver = driver;
    }

    public static void main(String[] args) {
        try {
            new Server(new FileSystemDriver()).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Метод используется для логирования событий происходящих в сервер-приложении
     */
    public static void logging(String message) {
        String time = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(System.currentTimeMillis());
        System.out.println(time + ": " + message);
    }


    /** Метод обрабатывает входящий пакет. На первом этапе определяется тип команды, затем
     * команда передается на исполнение драйверу, указанному при создании сервера или если
     * это запрос на подключение от нового пользователя - проверяется доступность имени и
     * формируется ответ. После пакет на отправку добавляется в очередь задач, откуда ее
     * заберет поток обрабатывающий задачи.
     * Данный метод вызывается из потоков-обработчиков задач: ReadableThread, WritableThread
     * */
    public void packetProcess(Packet packet, User user) {
        if (packet != null) {
            switch (packet.getCommandType()) {
                case CONNECT_TO_SERVER:
                    user.setName(packet.getText());
                    boolean add = false;
                    if (!activeUsers.contains(user)) {
                        add = activeUsers.add(user);
                        countActiveUsers.incrementAndGet();
                    }
                    if (add) {
                        user.setConnect(true);
                        user.setCurDirectory(VirtualFileSystem.getInstance().getRootName());
                        logging(String.format("В системе зарегистрировался новый пользователь - %s (%s), всего - %d", user.getName(), user.getRemoteAddress(), countActiveUsers.get()));

                        String message = user.getCurDirectory() + Protocol.SEPARATOR + String.format(Config.WELCOME_MESSAGE, countActiveUsers.get());
                        Packet packetServer = new Packet(CommandType.CONNECT_OK, user.getName(), message);
                        sendPacket(user, packetServer);
                    } else {
                        logging(String.format("Пользователю - %s (%s) отказано в регистрации, всего - %d", user.getName(), user.getRemoteAddress(), countActiveUsers.get()));
                        Packet packetServer = new Packet(CommandType.CONNECT_NO, user.getName(), String.format(Config.CONNECT_NO_MESSAGE, user.getName()));
                        sendPacket(user, packetServer);
                    }
                    user.getChannel().keyFor(selector).interestOps(SelectionKey.OP_READ);
                    break;
                case QUIT:
                    SocketChannel channel = user.getChannel();
                    if (channel != null) {
                        try {
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (user.isConnect()) {
                        user.setConnect(false);
                        activeUsers.remove(user);
                        countActiveUsers.decrementAndGet();
                        VirtualFileSystem.getInstance().unlockAllFilesByUser(user);
                        logging(String.format("Из системы вышел пользователь - %s (%s), всего - %d", user.getName(), user.getRemoteAddress(), countActiveUsers.get()));
                    }
                    break;
                default:
                    Packet packetServer = driver.execCommand(user, packet.getCommandType(), packet.getText());
                    sendPacket(user, packetServer);
                    if (packet.getCommandType() != CommandType.CD && packet.getCommandType() != CommandType.PRINT &&
                        packetServer.getCommandType() != CommandType.SERVER_ERROR) {
                        broadcastPacket(user, packet);
                    }
                    user.getChannel().keyFor(selector).interestOps(SelectionKey.OP_READ);
            }
            selector.wakeup();
        } else {
            Packet packetServer = new Packet(CommandType.SERVER_ERROR, user.getName(), Config.NOT_RECEIVED_COMMAND);
            sendPacket(user, packetServer);
        }
    }

    /** Метод реализует работу серверного-приложения. В цикле опрашиваются каналы передачи данных на возникновение
     * новых событий. Если поступил запрос на новое подключение метод создает соответствующий канал на стороне
     * сервера, на остальные события заводятся задачи и помещаются в очередь, откуда их забирает отдельный поток
     * ChannelTasksProcessor
     * */
    private void run() throws IOException, InterruptedException {
        logging("Сервер запущен!");
        new ChannelTasksProcessor(channelTasks).start();
        while (true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey selectionKey = keys.next();
                if (selectionKey.isValid()) {
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        try {
                            SocketChannel channel = serverSocketChannel.accept();
                            logging(String.format("Адрес %s запросил доступ к серверу", channel.getRemoteAddress()));
                            channel.configureBlocking(false);
                            channel.register(selector, SelectionKey.OP_READ);
                            User user = new User(channel.getRemoteAddress().toString(), channel);
                            channel.keyFor(selector).attach(user);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (selectionKey.isReadable()) {
                                ChannelTask task= new ChannelTask((User) selectionKey.attachment(), this, ChannelTaskType.READABLE);
                                channelTasks.put(task);
                                selectionKey.interestOps(0);
                                logging(String.format("В очередь добавлена задача на чтение, ID - %d", task.getId()));
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                keys.remove();
            }
        }
    }

    /** Метод формирует информативные сообщения для всех пользователей об измененииях в файловой системе и
     * помещает их в очередь на отправку
     * */
    private void broadcastPacket(User user, Packet packet) {
        String text = String.format("%s performs command: %s %s", user.getName(), packet.getCommandType().getName(), packet.getText());
        for (User u : activeUsers) {
            if (!u.equals(user)) {
                Packet broadcastPacket = new Packet(CommandType.SERVER_MESSAGE, u.getName(), text);
                sendPacket(u, broadcastPacket);
            }
        }
    }

    /** Метод помещает пакет в очередь на отправку
     * */
    private void sendPacket(User user, Packet packet) {
        ChannelTask task = new ChannelTask(user, this, ChannelTaskType.WRITABLE, packet);
        try {
            channelTasks.put(task);
            logging(String.format("В очередь добавлена задача на запись, ID = %d", task.getId()));
        } catch (InterruptedException e) {
            logging(String.format("В очередь не удалось добавить задачу на запись, ID = %d", task.getId()));
            e.printStackTrace();
        }

    }

    private class UserComparator implements Comparator<User> {
        @Override
        public int compare(User o1, User o2) {
            if (o1.hashCode() == o2.hashCode()) {
                return 0;
            } else if (o1.hashCode() < o2.hashCode()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}