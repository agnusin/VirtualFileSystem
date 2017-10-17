package ru.gnusinay.server;

import java.nio.channels.SocketChannel;
import java.util.Objects;

/** Класс описывающий пользователя сервер-приложения. Хранит
 * в себе имя пользователя, его текущую директорию, канал передачи
 * данных, удаленный адрес и состояние подключения.
 * */
public class User {
    private String name;
    private String curDirectory;
    private boolean isConnect;
    private SocketChannel channel;
    private String remoteAddress;

    public User() {
        this("", null);
    }

    public User(String name, SocketChannel channel) {
        this.name = name;
        this.isConnect = false;
        this.curDirectory = "";
        this.channel = channel;
        try {
            this.remoteAddress = channel.getRemoteAddress().toString().substring(1);
        } catch (Exception e) {
            this.remoteAddress = "";
        }
    }

    public String getCurDirectory() {
        return curDirectory;
    }

    public void setCurDirectory(String curDirectory) {
        this.curDirectory = curDirectory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj instanceof User) {
            return this.name.toLowerCase().equals(((User) obj).getName().toLowerCase());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name.toLowerCase());
    }


}
