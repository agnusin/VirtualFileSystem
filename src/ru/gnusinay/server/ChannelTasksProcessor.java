package ru.gnusinay.server;

import ru.gnusinay.protocol.Packet;
import ru.gnusinay.protocol.Protocol;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Класс-поток для обработки задач на чтение/запись в канал.
 * Запускается отдельно от основного потока сервер-приложения.
 * Внутри класса хранится блокирующая очередь, передаваемая в конструктор
 * класса, откуда поток забирает вновь поступившие задачи. Очередь
 * синхронизирована с основным потоком сервер-приложения. Одновременно
 * может исполняться несколько задач, в зависимости от настроек в
 * конфигурационном файле
 * */
public class ChannelTasksProcessor extends Thread {
    private BlockingQueue<ChannelTask> queue;

    public ChannelTasksProcessor(BlockingQueue<ChannelTask> queue) {
        this.queue = queue;
    }

    /** Метод забирает задачи из очереди, определяет тип задач и запускает обработку задачи
     * в отдельном потоке. Метод может запускать параллельно несколько задач
     * */
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(Config.TASK_WORKER_COUNT);
        while (true) {
            try {
                ChannelTask task = queue.take();
                if (task != null) {
                    Thread thread = getProcessor(task);
                    if (thread != null) {
                        Server.logging(String.format("ID - %d. Запуск задачи, поток - %s", task.getId(), Thread.currentThread().getName()));
                        executorService.execute(thread);
                    }
                }
            } catch (InterruptedException e) {
                Server.logging("ChannelTasksProcessor - " + e.getMessage());
            }
        }
    }

    /** Метод по типу задачи выбирает соответствующую реализацию класса-обработчика
     * */
    public Thread getProcessor(ChannelTask task) {
        switch (task.getType()) {
            case READABLE:
                return new ReadableThread(task);
            case WRITABLE:
                return new WritableThread(task);
            default: return null;
        }
    }
}

/** Класс-поток обрабатывающий задачи на чтение из канала. Хранит
 * внутри ссылку на обрабатываемую задачу.
 * */
class ReadableThread extends Thread {
    private ChannelTask task;

    ReadableThread(ChannelTask task) {
        this.task = task;
    }

    /** Метод считывает из канала данные. На первом этапе считываются
     * первые 4 байта сообщения, в которых содержится информация о длине
     * поступившего сообщения, затем создается буфер необходимого размера,
     * в котороый записывается сообщение. Далее сообщение декодируется в
     * пакет и обрабатывается.
     * */
    @Override
    public void run() {
        User user = task.getUser();
        SocketChannel socketChannel = task.getChannel();
        ByteBuffer readBuffer = ByteBuffer.allocate(4);
        int readByte = 0;
        try {
            readByte = socketChannel.read(readBuffer);
            if (readByte > 0) {
                int byteCount = Protocol.getPacketLength(readBuffer.array());
                readBuffer = ByteBuffer.allocate(byteCount);
                readByte = socketChannel.read(readBuffer);

            }
        } catch (IOException e) {
            readByte = -1;
        }
        if (readByte > 0) {
            Packet packet = Protocol.decode(readBuffer.array());
            Server.logging(String.format("ID - %d. Получен пакет '%s' от пользователя %s", task.getId(), packet.toString(), task.getUser().getName()));
            task.getServer().packetProcess(packet, user);
        } else if (readByte == -1) {
            Server.logging(String.format("Пользователь %s закрыл канал", user.getName()));
            task.getServer().packetProcess(Protocol.makeUnconnectedPacket(user), user);
        }
        Server.logging(String.format("ID - %d. Задача обработана, поток - %s", task.getId(), Thread.currentThread().getName()));
    }
}

/** Класс-поток обрабатывающий задачи на запись в канал. Хранит
 * внутри ссылку на обрабатываемую задачу.
 * */
class WritableThread extends Thread {
    private ChannelTask task;

    WritableThread(ChannelTask task) {
        this.task = task;
    }

    /** Метод записывает в канала данные. На первом этапе передаваемый
     * пакет кодируется и далее полученный поток байтов записывается в канал
     * */
    @Override
    public void run() {
        User user = task.getUser();
        SocketChannel socketChannel = task.getChannel();
        Packet packet = (Packet) task.getAttachment();
        try {
            if (socketChannel.isConnected()) {
                socketChannel.write(ByteBuffer.wrap(Protocol.encode(packet)));
                Server.logging(String.format("ID - %d. Пакет '%s' отправлен пользователю %s", task.getId(), packet.toString(), user.getName()));
            }
        } catch (IOException e) {
            {
                Server.logging(String.format("ID - %d. Пакет '%s' не удалось отправить пользователю %s", task.getId(), packet.toString(), user.getName()));
                e.printStackTrace();
            }
        }
        Server.logging(String.format("ID - %d. Задача обработана, поток - %s", task.getId(), Thread.currentThread().getName()));
    }
}
