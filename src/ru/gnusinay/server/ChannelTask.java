package ru.gnusinay.server;

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/** Класс описывает задачу на чтение/запись данных из канала.
 * Поля класса содержат всю необходимую информацию о том,
 * какому пользователлю относится задача, тип задачи - чтение/запись,
 * экземпляр сервера, с которого поступила задач.
 * Для идентификации каждой отдельной задачи предназначено поле id.
 * При формировании здачи на запись, к задаче прикрепляется пакет
 * который необходимо отправить пользователю
 * */
public class ChannelTask {
    private static AtomicInteger count = new AtomicInteger(0);

    private int id;
    private User user;
    private Server server;
    private ChannelTaskType type;
    private Object attachment;

    ChannelTask(User user, Server server, ChannelTaskType type) {
        this.id = count.incrementAndGet();
        this.user = user;
        this.server = server;
        this.type = type;
    }

    public ChannelTask(User user, Server server, ChannelTaskType type, Object attachment) {
        this.id = count.incrementAndGet();
        this.user = user;
        this.server = server;
        this.type = type;
        this.attachment = attachment;
    }

    public int getId() {
        return id;
    }

    public SocketChannel getChannel() {
        return user.getChannel();
    }

    public User getUser() {
        return user;
    }

    public ChannelTaskType getType() {
        return type;
    }

    public Server getServer() {
        return server;
    }

    public Object getAttachment() {
        return attachment;
    }

}
