package ru.gnusinay.vfs;

import ru.gnusinay.server.Config;
import ru.gnusinay.server.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/** Класс, реализующий файловую систему. Содержит корневой элемент системы и
 * карту заблокированных объектов в системе, для потокобезопасной работы.
 * Класс предоставляет методы для работы с файловой системой.
 * Реализует шаблон синглтон, т.к. файловая система должна существовать
 * только в одном экземпляре.
 * */
public class VirtualFileSystem {
    private static VirtualFileSystem fileSystem = new VirtualFileSystem();

    private Directory rootNode;
    private HashMap<String, List<Node>> lockNodes;

    private VirtualFileSystem() {
        rootNode = new Directory(Config.ROOT, null);
        lockNodes = new HashMap<>();
    }

    static public VirtualFileSystem getInstance() {
        return fileSystem;
    }

    public String getRootName() {
        return rootNode.getName();
    }



    public boolean addDirectory(String path) throws VFSException, InterruptedException {
        try {
            Directory directory = (Directory) findNode(path);
            if (directory == null) {
                String name = path.substring(path.lastIndexOf("\\") + 1);
                path = path.substring(0, path.lastIndexOf("\\"));
                Directory parent = (Directory) findNode(path);
                    if (parent != null) {
                        return parent.addChild(new Directory(name, parent));
                    } else {
                        throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
                    }
            }
            throw new VFSException(String.format(Config.DOUBLE_DIRECTORY_EXCEPTION_MESSAGE, directory.toString()));
        } finally {
            unlockNode();
        }
    }

    public boolean addFile(String path) throws VFSException, InterruptedException {
        try {
            File file = (File) findNode(path);
            if (file == null) {
                String name = path.substring(path.lastIndexOf("\\") + 1);
                path = path.substring(0, path.lastIndexOf("\\"));
                Directory parent = (Directory) findNode(path);
                if (parent != null) {
                    return parent.addChild(new File(name, parent));
                } else {
                    throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
                }
            }
            throw new VFSException(String.format(Config.DOUBLE_FILE_EXCEPTION_MESSAGE, file.toString()));
        } finally {
            unlockNode();
        }
    }

    public boolean deleteDirectory(String path) throws VFSException, InterruptedException {
        try {
            Directory node = (Directory) findNode(path);
            if (node != null) {
                if (!node.hasChildrenDirectory()) {
                    return _deleteDirectory(node);
                } else {
                    throw new VFSException(String.format(Config.DIRECTORY_HAS_OTHER_DIRECTORIES_EXCEPTION_MESSAGE, node.toString()));
                }
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
            }
        } finally {
            unlockNode();
        }
    }

    public boolean deleteFile(String path) throws VFSException, InterruptedException {
        try {
            File node = (File) findNode(path);
            if (node != null) {
                return _deleteFile(node);
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
            }
        } finally {
            unlockNode();
        }
    }


    public boolean deleteDirectoryTree(String path) throws VFSException, InterruptedException {
        try {
            Directory node = (Directory) findNode(path);
            if (node != null) {
                return _deleteDirectory(node);
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
            }
        } finally {
            unlockNode();
        }
    }

    public boolean lockFile(String path, User user) throws VFSException, InterruptedException {
        try {
            File node = (File) findNode(path);
            if (node != null) {
                if (!node.isLock()) {
                    return node.lock(user);
                } else {
                    throw new VFSException(String.format(Config.FILE_LOCKED_EXCEPTION_MESSAGE, path));
                }
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
            }
        } finally {
            unlockNode();
        }
    }

    public boolean unlockFile(String path, User user) throws VFSException, InterruptedException {
        try {
            File node = (File) findNode(path);
            if (node != null) {
                if (node.isLock()) {
                    return node.unlock(user);
                }
                throw new VFSException(String.format(Config.NOT_LOCKED_FILE_EXCEPTION_MESSAGE, node.toString()));
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
            }
        } finally {
            unlockNode();
        }
    }

    /** Метод предназначен для разблокировки всех файлов пользователя. Применяется при отключении
     * пользователя от системы
     */
    public void unlockAllFilesByUser(User user) {
        _unlockAllFilesByUser(rootNode, user);
    }

    public boolean copyNode(String pathFrom, String pathTo) throws VFSException, InterruptedException {
        try {
            Node node = findNode(pathFrom);
            if (node != null) {
                if (node.getType() == NodeType.DIRECTORY && ((Directory) node).isRoot()) {
                    throw new VFSException(String.format(Config.ROOT_NODE_CHANGE_EXCEPTION_MESSAGE, node.toString()));
                }
                Directory nodeTo = (Directory) findNode(pathTo);
                if (nodeTo != null) {
                    return nodeTo.addChild(cloneNode(node, nodeTo));
                } else {
                    throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, pathTo));
                }
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, pathFrom));
            }
        } finally {
            unlockNode();
        }
    }

    public boolean moveNode(String pathFrom, String pathTo) throws VFSException, InterruptedException {
        try {
            Node node = findNode(pathFrom);
            if (node != null) {
                if (node.getType() == NodeType.DIRECTORY) {
                    Directory directoryTo = (Directory) findNode(pathTo);
                    if (directoryTo != null) {
                        if (_deleteDirectory((Directory) node)) {
                            Directory oldParent = (Directory) node.getParent();
                            node.setParent(directoryTo);
                            if (directoryTo.addChild(node)) {
                                return true;
                            } else {
                                node.setParent(oldParent);
                                oldParent.addChild(node);
                                return false;
                            }
                        } else {
                            return false;
                        }

                    } else {
                        throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, pathTo));
                    }
                } else {
                    if (!((File) node).isLock()) {
                        Directory directoryTo = (Directory) findNode(pathTo);
                        if (directoryTo != null) {
                            node.setParent(directoryTo);
                            return directoryTo.addChild(node);
                        } else {
                            throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, pathTo));
                        }
                    } else {
                        throw new VFSException(String.format(Config.LOCKED_FILE_EXCEPTION_MESSAGE, node.toString()));
                    }
                }
            } else {
                throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, pathFrom));
            }
        } finally {
            unlockNode();
        }
    }

    public String print() {
        if (rootNode.hasChildren()) {
            StringBuilder builder = new StringBuilder(rootNode.getName()).append('\n');
            _print(rootNode, builder, 1);
            return builder.toString().replaceAll("\\n(\\|([\\| ])+\\n)+(?=(\\| )+\\n(\\| )*\\|_)|([\\| \\n]+$)", "\n");
        }
        return rootNode.getName() + '\n';
    }

    /** Метод проверяет наличие элемента в файловой системе по входному пути
     * */
    public String checkPath(String path) throws VFSException, InterruptedException {
        try {
            Node node = findNode(path);
            if (node != null) {
                return node.toString();
            }
            throw new VFSException(String.format(Config.NOT_FOUND_NODE_EXCEPTION_MESSAGE, path));
        } finally {
            unlockNode();
        }
    }

    private Node findNode(String path) throws VFSException, InterruptedException {
        List<Node> lockNodeList = lockNodes.get(Thread.currentThread().getName());
        if (lockNodeList == null) {
            lockNodeList = new ArrayList<>();
            lockNodes.put(Thread.currentThread().getName(), lockNodeList);
        }
        String[] parts = path.split("\\\\");
        Node curNode = rootNode;
        if (parts.length > 0 && parts[0].equalsIgnoreCase(rootNode.getName())) {
            for (int i = 1; i < parts.length; i++) {
                if (curNode != null && curNode.getType() == NodeType.DIRECTORY && ((Directory) curNode).hasChildren()) {
                    Iterator<Node> iterator = ((Directory) curNode).getChildren().iterator();
                    curNode = null;
                    while (iterator.hasNext()) {
                        Node node = iterator.next();
                        if (parts[i].equalsIgnoreCase(node.getName())) {
                            curNode = node;
                            if (!lockNodeList.contains(curNode)) {
                                if (curNode.lockNode()) {
                                    lockNodeList.add(curNode);
                                    break;
                                } else {
                                    throw new VFSException(Config.RUN_COMMAND_INTERRUPTED_EXCEPTION);
                                }
                            }
                        }
                    }
                } else {
                    return null;
                }
            }
            return curNode;
        }
        return null;
    }

    private void unlockNode() throws InterruptedException {
        List<Node> curLockedNodes = lockNodes.get(Thread.currentThread().getName());
        if (curLockedNodes != null) {
            Iterator<Node> nodeIterator = curLockedNodes.iterator();
            while (nodeIterator.hasNext()) {
                Node n = nodeIterator.next();
                if (n.isLockedNode()) {
                    n.unlockNode();
                    nodeIterator.remove();
                }
            }
        }
    }

    private boolean hasUserLockedFiles(Directory node) {
        for (Node n : node.getChildren()) {
            if (n.getType() == NodeType.FILE) {
                if (((File) n).isLock()) {
                    return true;
                }
            } else {
                return hasUserLockedFiles((Directory) n);
            }
        }
        return false;
    }

    private Node cloneNode(Node cloneNode, Directory newParent) {
        switch (cloneNode.getType()) {
            case DIRECTORY:
                Directory cloneDirectory = new Directory(cloneNode.getName(), newParent);
                if (((Directory)cloneNode).hasChildrenDirectory()) {
                    for (Node node : ((Directory) cloneNode).getChildren()) {
                        cloneDirectory.addChild(cloneNode(node, cloneDirectory));
                    }
                }
                return cloneDirectory;
            case FILE:
                File cloneFile = new File(cloneNode.getName(), newParent);
                for (User user : ((File) cloneNode).getLockUsers()) {
                    cloneFile.lock(user);
                }
                return cloneFile;
            default:;
        }
        return null;
    }


    private boolean _deleteDirectory(Directory directory) throws VFSException {
        if (!directory.isRoot()) {
            if (!hasUserLockedFiles(directory)) {
                Directory parent = directory.getParent();
                return parent.removeChild(directory);
            } else {
                throw new VFSException(String.format(Config.LOCKED_FILES_IN_DIRECTORY_EXCEPTION_MESSAGE, directory.toString()));
            }
        } else {
            throw new VFSException(String.format(Config.ROOT_NODE_CHANGE_EXCEPTION_MESSAGE,  directory.toString()));
        }
    }

    private boolean _deleteFile(File file) throws VFSException {
        if (!file.isLock()) {
            Directory parent = file.getParent();
            return parent.removeChild(file);
        } else {
            throw new VFSException(String.format(Config.LOCKED_FILE_EXCEPTION_MESSAGE, file.toString()));
        }
    }

    private void _unlockAllFilesByUser(Node node, User user) {
        if (node.getType() == NodeType.FILE) {
            ((File) node).unlock(user);
        } else {
            for (Node n : ((Directory) node).getChildren()) {
                _unlockAllFilesByUser(n, user);
            }
        }
    }

    private void _print(Directory node, StringBuilder builder, int level) {
        for (Node n : node.getChildren()) {
            for (int i = 0; i < level; i++) {
                builder.append("| ");
            }
            builder.replace(builder.length() - 1, builder.length(), "_").append(n.getName()).append('\n');

            if (n.getType() == NodeType.DIRECTORY) {
                _print((Directory) n, builder, level + 1);
                for (int i = 0; i < level; i++) {
                    builder.append("| ");
                }
                builder.append('\n');
            } else if (((File) n).isLock()) {
                builder.replace(builder.length() - 1, builder.length(), " [LOCKED");
                for (User user : ((File) n).getLockUsers()) {
                    builder.append(" ").append(user.getName()).append(",");
                }
                builder.replace(builder.length() - 1, builder.length(), "]\n");
            }
        }
    }
}
