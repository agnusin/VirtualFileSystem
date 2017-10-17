package ru.gnusinay.server;

import ru.gnusinay.client.CommandType;
import ru.gnusinay.protocol.Packet;
import ru.gnusinay.protocol.Protocol;
import ru.gnusinay.vfs.VFSException;
import ru.gnusinay.vfs.VirtualFileSystem;

/** Класс является связкой между сервером и файловой системой.
 * Предназначен для выполнения пользовтельских команд над
 * файловой системой и генерации соответствующих сообщений для
 * пользователей.
 */
public class FileSystemDriver implements CommandDriver {

    /** Метод выполняет команду и возвращает пакет с результатом выполнения
     * */
    public Packet execCommand(User user, CommandType type, String params) {
        try {
            switch (type) {
                case CD:
                    String homeDir = VirtualFileSystem.getInstance().checkPath(params);
                    user.setCurDirectory(homeDir);
                    return new Packet(CommandType.CHANGE_HOME_DIR, user.getName(), homeDir);
                case MD:
                    if (VirtualFileSystem.getInstance().addDirectory(params)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.CREATE_DIRECTORY_MESSAGE);
                    }
                    break;
                case MF:
                    if (VirtualFileSystem.getInstance().addFile(params)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.CREATE_FILE_MESSAGE);
                    }
                    break;
                case RD:
                    if (user.getCurDirectory().toLowerCase().contains(params.toLowerCase())) {
                        return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE);
                    } else if (VirtualFileSystem.getInstance().deleteDirectory(params)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.REMOVE_DIRECTORY_MESSAGE);
                    }
                    break;
                case DELTREE:
                    if (user.getCurDirectory().toLowerCase().contains(params.toLowerCase())) {
                        return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE);
                    } else if (VirtualFileSystem.getInstance().deleteDirectoryTree(params)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.REMOVE_TREE_DIRECTORY_MESSAGE);
                    }
                    break;
                case DEL:
                    if (VirtualFileSystem.getInstance().deleteFile(params)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.REMOVE_FILE_MESSAGE);
                    }
                    break;
                case LOCK:
                    if (VirtualFileSystem.getInstance().lockFile(params, user)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.LOCK_FILE_MESSAGE);
                    }
                    break;
                case UNLOCK:
                    if (VirtualFileSystem.getInstance().unlockFile(params, user)) {
                        return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.UNLOCK_FILE_MESSAGE);
                    }
                    break;
                case MOVE:
                    String[] moveParams = params.split(Protocol.SEPARATOR);
                    if (moveParams.length == 2) {
                        if (user.getCurDirectory().toLowerCase().contains(moveParams[0].toLowerCase())) {
                            return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.REMOVE_HOME_DIRECTORY_EXCEPTION_MESSAGE);
                        } else if (VirtualFileSystem.getInstance().moveNode(moveParams[0], moveParams[1])) {
                            return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.MOVE_NODE_MESSAGE);
                        }
                        return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.NOT_MOVED_NODE_MESSAGE);
                    }
                    break;
                case COPY:
                    String[] copyParams = params.split(Protocol.SEPARATOR);
                    if (copyParams.length == 2) {
                        if (VirtualFileSystem.getInstance().copyNode(copyParams[0], copyParams[1])) {
                            return new Packet(CommandType.SERVER_RESPONSE, user.getName(), Config.COPY_NODE_MESSAGE);
                        }
                        return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.NOT_COPIED_NODE_MESSAGE);
                    }
                    break;
                case PRINT:
                    String message = VirtualFileSystem.getInstance().print();
                    return new Packet(CommandType.SERVER_RESPONSE, user.getName(), '\n' + message);
                default:
                    return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.NOT_SUPPORTED_COMMAND);
            }
        } catch (VFSException e) {
            return new Packet(CommandType.SERVER_ERROR, user.getName(), e.getMessage());
        } catch (InterruptedException e) {
            Server.logging("FileSystemDriver - " + e.getMessage());
            return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.RUN_COMMAND_INTERRUPTED_EXCEPTION);
        }

        return new Packet(CommandType.SERVER_ERROR, user.getName(), Config.NOT_EXECUTED_COMMAND);
    }
}