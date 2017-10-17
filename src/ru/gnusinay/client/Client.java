package ru.gnusinay.client;

import ru.gnusinay.protocol.Packet;
import ru.gnusinay.protocol.Protocol;
import ru.gnusinay.server.Config;
import ru.gnusinay.server.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/** Класс реализует клиент-приложение. При подключении к серверу
 * приложение в цикле начинает опрашивать канал на возникновении новых событий
 * и реагирует на них. Также в отдельном потоке запускается ConsoleListener,
 * который опрашивает консоль на ввод новых команд от пользователя. Введенные команды
 * считываются из очереди и отправляются на сервер на выполнения.
 * Семафор позволяет реализовать режим запрос-ответ между двумя потоками.
 * */
public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(Config.WELCOME_CLIENT_MESSAGE);
            scanner.reset();
            String line = scanner.nextLine();
            CommandType type = CommandParser.match(line);
            switch (type) {
                case CONNECT_TO_SERVER:
                    String[] param = CommandParser.getParameters(type, line, "");
                    try {
                        new Client().run(param[0], Integer.valueOf(param[1]), param[2]);
                    } catch (IOException e) {
                        System.out.println();
                        System.out.println(Config.NO_CONNECT_CLIENT_MESSAGE);
                        continue;
                    }
                    break;
                case QUIT:
                    System.exit(0);
                    break;
                case UNDEFINED:
                    System.out.println(Config.INCORRECT_COMMAND_CLIENT_MESSAGE);
                    break;
                default:;
            }
        }
    }


    /** Метод завершает процесс подключения к серверу и в случае успешного подключения
     * начинает опрашивать канал на возникновение событий. Если в канале появились данные,
     * метод выводит их на консоль. При появлении новой команды в очереди, формируется
     * пакет и отправляется серверу
     */
    private void run(String address, int port, String userName) throws IOException {
        SocketChannel channel = SocketChannel.open();
        Selector selector = Selector.open();
        Queue<Packet> queue = new ArrayDeque<>(1);
        User user = new User(userName, channel);
        Semaphore semaphore = new Semaphore(1);
        ConsoleListener consoleListener = new ConsoleListener(user, selector, queue, semaphore);
        try {
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(address, port));

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey selectionKey = keys.next();
                    keys.remove();

                    if (selectionKey.isConnectable()) {
                        channel.finishConnect();
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                        queue.add(Protocol.makeConnectPacket(user));

                    } else if (selectionKey.isReadable()) {

                        Packet packet = readPacket(channel);

                        if (packet != null) {
                            switch (packet.getCommandType()) {
                                case CONNECT_OK:
                                    String[] params = packet.getText().split(Protocol.SEPARATOR);

                                    user.setConnect(true);
                                    user.setCurDirectory(params[0]);
                                    System.out.println(String.format("%s> %s", params[0], params[1]));
                                    System.out.print(params[0] + "> ");
                                    consoleListener.start();
                                    break;
                                case CONNECT_NO:
                                    System.out.println(packet.getText());
                                    throw new IOException();
                                case CHANGE_HOME_DIR:
                                    user.setCurDirectory(packet.getText());
                                    System.out.print(String.format("%s> ", user.getCurDirectory()));
                                    semaphore.release();
                                    break;
                                case SERVER_RESPONSE:
                                    System.out.println(packet.getText());
                                    System.out.print(String.format("%s> ", user.getCurDirectory()));
                                    semaphore.release();
                                    break;
                                case SERVER_MESSAGE:
                                    System.out.println(packet.getText());
                                    System.out.print(String.format("%s> ", user.getCurDirectory()));
                                    break;
                                case SERVER_ERROR:
                                    System.out.println(packet.getText());
                                    System.out.print(String.format("%s> ", user.getCurDirectory()));
                                    semaphore.release();
                                    break;
                                default:;
                            }
                        }
                    } else if (selectionKey.isWritable()) {
                        Packet packet = queue.poll();
                        if (packet != null) {
                            channel.write(ByteBuffer.wrap(Protocol.encode(packet)));
                        }
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch(IOException e) {
            consoleListener.interrupt();
            throw new IOException(e);
        } finally {
            user.setConnect(false);
            channel.close();
            selector.close();
        }
    }

    private Packet readPacket(SocketChannel channel) throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(4);
        int readByte = 0;
        readByte = channel.read(readBuffer);
        if (readByte > 0) {
            int byteCount = Protocol.getPacketLength(readBuffer.array());
            readBuffer = ByteBuffer.allocate(byteCount);
            readByte = channel.read(readBuffer);
        }
        if (readByte > 0) {
            return Protocol.decode(readBuffer.array());
        }
        return null;
    }
}




