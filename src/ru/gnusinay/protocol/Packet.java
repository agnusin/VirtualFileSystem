package ru.gnusinay.protocol;

import ru.gnusinay.client.CommandType;

/** Класс, описывающий сообщения, которыми
 * обмениваются клиент-сервер. Состоит из
 * кода команды, имени пользователя и текста
 * (параметров) команды
 * */
public class Packet {
    private String commandCode;
    private String userName;
    private String text;

    public Packet(CommandType commandType, String userName) {
        this.commandCode = commandType.getCode();
        this.userName = userName;
        this.text = "";
    }

    public Packet(CommandType commandType, String userName, String text) {
        this.commandCode = commandType.getCode();
        this.userName = userName;
        this.text = text;
    }

    public String getCommandCode() {
        return commandCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CommandType getCommandType() {
        return CommandType.getCommandTypeByCode(commandCode);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getCommandCode()).append(Protocol.SEPARATOR);
        builder.append(this.getUserName());
        if (!this.getText().isEmpty()) {
            builder.append(Protocol.SEPARATOR).append(this.getText());
        }
        return builder.toString();
    }
}
