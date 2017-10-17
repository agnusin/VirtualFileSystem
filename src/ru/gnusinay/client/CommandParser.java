package ru.gnusinay.client;

import ru.gnusinay.protocol.Protocol;
import java.util.regex.Pattern;

/** Класс позволяет проверить правильность
 * ввода команд пользователем и получить
 * параметры команд
 * */
public class CommandParser {


    /** Метод разбирает входящую строку и возвращает массив параметров в
     * виде полного пути к каждому элементу файловой системы
     * */
    static public String[] getParameters(CommandType type, String commandLine, String homeDirectory) {
        if (!"".equals(type.getParamSeparator())) {
            String[] params = commandLine.replaceAll("(^\\s*\\w+\\s+)|(\\s+)", " ").trim().split(type.getParamSeparator());
            if (params.length > 0 && type != CommandType.CONNECT_TO_SERVER) {
                for (int i = 0; i < params.length; i++) {
                    if ("".equals(params[i])) {
                        params[i] = homeDirectory;
                    } else if (!params[i].contains(":")) {
                        params[i] = new String(homeDirectory + "\\" + params[i]).replaceAll("\\\\(?=\\\\)|(\\\\$)", "");
                    }
                }
            }
            return params;
        }
        return new String[0];
    }

    /** Метод возвращает строку с разделенными параметрами
     * */
    static public String getParametersAsLine(CommandType type, String commandLine, String homeDirectory) {
        String[] params = getParameters(type, commandLine, homeDirectory);
        if (params.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                builder.append(params[i]).append(Protocol.SEPARATOR);
            }
            return builder.toString();
        }
        return "";
    }

    /** Метод проверяет правильность ввода команды и возвращает тип введенной команды.
     * Если не удалось поределить команду, то возвращается тип UNDEFINED
     * */
    static public CommandType match(String line) {
        if (Pattern.compile("^[A-Za-z0-9 :._\\\\]+$").matcher(line).matches()) {
            if (line != null && !line.isEmpty()) {
                for (CommandType type : CommandType.values()) {
                    if (Pattern.compile(type.getRegexp()).matcher(line).matches()) {
                        return type;
                    }
                }
            }
        }
        return CommandType.UNDEFINED;
    }
}


