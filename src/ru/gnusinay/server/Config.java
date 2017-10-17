package ru.gnusinay.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static int      SERVER_PORT;
    public static String   SERVER_ADDRESS;
    public static int      TASK_QUEUE_SIZE;
    public static String   WELCOME_MESSAGE;
    public static String   CONNECT_NO_MESSAGE;
    public static String   NOT_RECEIVED_COMMAND;
    public static int      TASK_WORKER_COUNT;
    public static String   CREATE_DIRECTORY_MESSAGE;
    public static String   CREATE_FILE_MESSAGE;
    public static String   REMOVE_DIRECTORY_MESSAGE;
    public static String   REMOVE_TREE_DIRECTORY_MESSAGE;
    public static String   REMOVE_FILE_MESSAGE;
    public static String   LOCK_FILE_MESSAGE;
    public static String   UNLOCK_FILE_MESSAGE;
    public static String   MOVE_NODE_MESSAGE;
    public static String   NOT_MOVED_NODE_MESSAGE;
    public static String   COPY_NODE_MESSAGE;
    public static String   NOT_COPIED_NODE_MESSAGE;
    public static String   NOT_SUPPORTED_COMMAND;
    public static String   NOT_EXECUTED_COMMAND;
    public static String   ROOT;
    public static String   DOUBLE_DIRECTORY_EXCEPTION_MESSAGE;
    public static String   DOUBLE_FILE_EXCEPTION_MESSAGE;
    public static String   LOCKED_FILES_IN_DIRECTORY_EXCEPTION_MESSAGE;
    public static String   LOCKED_FILE_EXCEPTION_MESSAGE;
    public static String   NOT_FOUND_NODE_EXCEPTION_MESSAGE;
    public static String   ROOT_NODE_CHANGE_EXCEPTION_MESSAGE;
    public static String   REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE;
    public static String   WELCOME_CLIENT_MESSAGE;
    public static String   NO_CONNECT_CLIENT_MESSAGE;
    public static String   INCORRECT_COMMAND_CLIENT_MESSAGE;
    public static String   NOT_SENT_PACKET_CLIENT_EXCEPTION_MESSAGE;
    public static String   DIRECTORY_HAS_OTHER_DIRECTORIES_EXCEPTION_MESSAGE;
    public static String   NOT_LOCKED_FILE_EXCEPTION_MESSAGE;
    public static String   RUN_COMMAND_INTERRUPTED_EXCEPTION;
    public static String   FILE_LOCKED_EXCEPTION_MESSAGE;

    private static final String PROPERTIES_FILE = ".\\ru\\gnusinay\\server\\config.properties";

    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;

        try {
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);

            SERVER_PORT = Integer.parseInt(properties.getProperty("SERVER_PORT"));
            SERVER_ADDRESS = properties.getProperty("SERVER_ADDRESS");
            ROOT = properties.getProperty("ROOT");
            TASK_QUEUE_SIZE = Integer.parseInt(properties.getProperty("TASK_QUEUE_SIZE"));
            WELCOME_MESSAGE = properties.getProperty("WELCOME_MESSAGE");
            CONNECT_NO_MESSAGE = properties.getProperty("CONNECT_NO_MESSAGE");
            NOT_RECEIVED_COMMAND = properties.getProperty("NOT_RECEIVED_COMMAND");
            TASK_WORKER_COUNT = Integer.parseInt(properties.getProperty("TASK_WORKER_COUNT"));
            CREATE_DIRECTORY_MESSAGE = properties.getProperty("CREATE_DIRECTORY_MESSAGE");
            CREATE_FILE_MESSAGE = properties.getProperty("CREATE_FILE_MESSAGE");
            REMOVE_DIRECTORY_MESSAGE = properties.getProperty("REMOVE_DIRECTORY_MESSAGE");
            REMOVE_TREE_DIRECTORY_MESSAGE = properties.getProperty("REMOVE_TREE_DIRECTORY_MESSAGE");
            REMOVE_FILE_MESSAGE = properties.getProperty("REMOVE_FILE_MESSAGE");
            LOCK_FILE_MESSAGE = properties.getProperty("LOCK_FILE_MESSAGE");
            UNLOCK_FILE_MESSAGE = properties.getProperty("UNLOCK_FILE_MESSAGE");
            MOVE_NODE_MESSAGE = properties.getProperty("MOVE_NODE_MESSAGE");
            NOT_MOVED_NODE_MESSAGE = properties.getProperty("NOT_MOVED_NODE_MESSAGE");
            COPY_NODE_MESSAGE = properties.getProperty("COPY_NODE_MESSAGE");
            NOT_COPIED_NODE_MESSAGE = properties.getProperty("NOT_COPIED_NODE_MESSAGE");
            NOT_SUPPORTED_COMMAND = properties.getProperty("NOT_SUPPORTED_COMMAND");
            NOT_EXECUTED_COMMAND = properties.getProperty("NOT_EXECUTED_COMMAND");
            DOUBLE_DIRECTORY_EXCEPTION_MESSAGE = properties.getProperty("DOUBLE_DIRECTORY_EXCEPTION_MESSAGE");
            DOUBLE_FILE_EXCEPTION_MESSAGE = properties.getProperty("DOUBLE_FILE_EXCEPTION_MESSAGE");
            LOCKED_FILES_IN_DIRECTORY_EXCEPTION_MESSAGE = properties.getProperty("LOCKED_FILES_IN_DIRECTORY_EXCEPTION_MESSAGE");
            LOCKED_FILE_EXCEPTION_MESSAGE = properties.getProperty("LOCKED_FILE_EXCEPTION_MESSAGE");
            NOT_FOUND_NODE_EXCEPTION_MESSAGE = properties.getProperty("NOT_FOUND_NODE_EXCEPTION_MESSAGE");
            ROOT_NODE_CHANGE_EXCEPTION_MESSAGE = properties.getProperty("ROOT_NODE_CHANGE_EXCEPTION_MESSAGE");
            REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE = properties.getProperty("REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE");
            WELCOME_CLIENT_MESSAGE = properties.getProperty("WELCOME_CLIENT_MESSAGE");
            NO_CONNECT_CLIENT_MESSAGE = properties.getProperty("NO_CONNECT_CLIENT_MESSAGE");
            INCORRECT_COMMAND_CLIENT_MESSAGE = properties.getProperty("INCORRECT_COMMAND_CLIENT_MESSAGE");
            NOT_SENT_PACKET_CLIENT_EXCEPTION_MESSAGE = properties.getProperty("NOT_SENT_PACKET_CLIENT_EXCEPTION_MESSAGE");
            DIRECTORY_HAS_OTHER_DIRECTORIES_EXCEPTION_MESSAGE = properties.getProperty("DIRECTORY_HAS_OTHER_DIRECTORIES_EXCEPTION_MESSAGE");
            NOT_LOCKED_FILE_EXCEPTION_MESSAGE = properties.getProperty("NOT_LOCKED_FILE_EXCEPTION_MESSAGE");
            RUN_COMMAND_INTERRUPTED_EXCEPTION = properties.getProperty("RUN_COMMAND_INTERRUPTED_EXCEPTION");
            FILE_LOCKED_EXCEPTION_MESSAGE = properties.getProperty("FILE_LOCKED_EXCEPTION_MESSAGE");


        } catch (FileNotFoundException e) {
            System.out.println("Error! File 'config.properties' not found! " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error! File 'config.properties' can not be read! " + e.getMessage());
        } finally {
            try {
                propertiesFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}