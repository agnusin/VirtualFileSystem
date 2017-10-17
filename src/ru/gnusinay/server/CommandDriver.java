package ru.gnusinay.server;

import ru.gnusinay.client.CommandType;
import ru.gnusinay.protocol.Packet;

/** Интерфейс описывает API драйверов, используемых сервером
 * для выполнения пользовательских команд
 * */
public interface CommandDriver {

    Packet execCommand(User user, CommandType type, String params);
}
