package ru.gnusinay.client;

/** Класс описывает все доступные команды, которые может выполнять сервер.
 * Также хранится регулярное выражение для каждой команды, по которому
 * можно определить правильность ввода команды перед отправкой на сервер
 * */
public enum CommandType {
    CONNECT_TO_SERVER("connect", "(?i)^(connect)\\s+\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:{1}\\d{4,5}\\s+\\w+( ){0,}$", ":|\\s", "10"),


    MD("md", "(?i)^(md)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){1,}( ){0,}$", "\\s", "11"),
    CD("cd", "(?i)^(cd)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}( ){0,}$", "\\s", "12"),
    RD("rd", "(?i)^(rd)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}( ){0,}$", "\\s", "13"),
    DELTREE("deltree", "(?i)^(deltree)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}( ){0,}$", "\\s", "14"),
    MF("mf", "(?i)^(mf)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){1}( ){0,}$", "\\s", "15"),
    DEL("del", "(?i)^(del)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){1}( ){0,}$", "\\s", "16"),
    LOCK("lock", "(?i)^(lock)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){1}( ){0,}$", "\\s", "17"),
    UNLOCK("unlock", "(?i)^(unlock)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){1}( ){0,}$", "\\s", "18"),
    COPY("copy", "(?i)^(copy)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){0,}\\s+([A-Za-z]{1}:\\\\|\\\\|\\w|\\w\\\\){1}(\\w+\\\\|\\w){0,}( ){0,}$", "\\s", "19"),
    MOVE("move", "(?i)^(move)\\s+([A-Za-z]{1}:\\\\|\\\\){0,1}(\\w+\\\\|\\w){0,}(\\w+\\.\\w+){0,}\\s+([A-Za-z]{1}:\\\\|\\\\|\\w|\\w\\\\){1}(\\w+\\\\|\\w){0,}( ){0,}$", "\\s", "20"),
    PRINT("print", "(?i)^(print){0,}$", "", "21"),


    QUIT("quit", "(?i)^(quit)$", "", "22"),
    CONNECT_OK("connect_ok", "", "", "23"),
    CONNECT_NO("connect_no", "", "", "24"),
    SERVER_RESPONSE("server_message", "", "", "25"),
    SERVER_MESSAGE("server_message", "", "", "26"),
    SERVER_ERROR("server_error", "", "", "27"),
    CHANGE_HOME_DIR("change_home_dir", "", "", "28"),
    UNDEFINED("", "", "", "0");
    
    private String name;
    private String regexp;
    private String paramSeparator;
    private String code;

    CommandType(String name, String regexp, String paramSeparator, String code) {
        this.name = name;
        this.regexp = regexp;
        this.paramSeparator = paramSeparator;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getParamSeparator() {
        return paramSeparator;
    }

    public String getCode() {
        return code;
    }

    public static CommandType getCommandTypeByCode(String code) {
        for(CommandType type : CommandType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
