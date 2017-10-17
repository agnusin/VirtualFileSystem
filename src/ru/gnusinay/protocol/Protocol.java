package ru.gnusinay.protocol;

import ru.gnusinay.client.CommandType;
import ru.gnusinay.server.User;
import java.nio.charset.Charset;

/** Класс реализует протокол передачи сообщения между
 * клиентом и сервером. Содержит методы колдирования
 * и декодирования передаваемых ообщений
 * */
public class Protocol {
    static public final String SEPARATOR = "%";


    /** Метод кодирует сообщение в масссив байтов
     * В первые 4 байта кодированого сообщения
     * записывается количество байтов в сообщении.
     * Сообщения клиентом и сервером записываются
     * в ASCII символах, которые в кодировке UTF-8
     * записываются 1 байтом. Части пакета в
     * кодированом сообщении отделяются задаваемым
     * разделителем.
     *
     * @param packet
     *          Пакет, который кодируется для передачи
     *
     * @return Массив байтов для записи в канал
     * */
    static public byte[] encode(Packet packet) {
        StringBuilder builder = new StringBuilder();
        builder.append("0000");
        builder.append(packet.getCommandCode()).append(SEPARATOR);
        builder.append(packet.getUserName());
        if (!packet.getText().isEmpty()) {
            builder.append(SEPARATOR).append(packet.getText());
        }
        byte[] message = builder.toString().getBytes(Charset.forName("UTF-8"));
        byte[] countByte = intToByteArray(message.length - 4);
        for(int i = 0; i < 4; i++) {
            message[i] = countByte[i];
        }
        return message;
    }

    /** Метод декодирует полученный поток байтов в сообщение
     * */
    static public Packet decode(byte[] buffer) {
        String message = new String(buffer, Charset.forName("UTF-8"));
        String[] parts = message.split(SEPARATOR);
        try {
            Packet packet = new Packet(CommandType.getCommandTypeByCode(parts[0]), parts[1]);
            if (parts.length > 2) {
                StringBuilder builder = new StringBuilder(parts[2]);
                for(int i = 3; i < parts.length; i++) {
                    builder.append(SEPARATOR).append(parts[i]);
                }
                packet.setText(builder.toString());
            }
            return packet;
        } catch (Exception e) {
            return new Packet(CommandType.UNDEFINED, "", e.getMessage());
        }
    }

    /** Метод декодирует переданные байты, в которых хранится количество
     * символов в пакете
     * */
    static public int getPacketLength(byte[] num) {
        return byteArrayToInt(num);
    }

    /** Медот-хелпер, генерирующий пакет для подключения к серверу
     * */
    static public Packet makeConnectPacket(User user) {
        return new Packet(CommandType.CONNECT_TO_SERVER, user.getName(), user.getName());
    }

    /** Метод-хелпер, генерирующий пакет отключения от сервера
     * */
    static public Packet makeUnconnectedPacket(User user) {
        return new Packet(CommandType.QUIT, user.getName());
    }

    static private int byteArrayToInt(byte[] b) {
        int dt = 0;
        if ((b[0] & 0x80) != 0)
            dt = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++)
            dt = (dt << 8) + (b[i] & 255);
        return dt;
    }

    static private byte[] intToByteArray(int n) {
        byte[] res = new byte[4];
        for (int i = 0; i < 4; i++)
            res[4 - i - 1] = (byte) ((n >> i * 8) & 255);
        return res;
    }
}
