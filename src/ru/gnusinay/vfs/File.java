package ru.gnusinay.vfs;

import ru.gnusinay.server.User;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/** Класс описывающий файл в файловой системе. Хранит
 * список всех пользователей, которые заблокировали файл
 */
public class File extends Node {
    private Set<User> lockUsers;

    public File(String name, Directory parent) {
        super(name, parent);
        this.lockUsers = new ConcurrentSkipListSet<>(new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });

    }

    public boolean lock(User user) {
        return lockUsers.add(user);
    }

    public boolean unlock(User user) {
        return lockUsers.remove(user);
    }

    public boolean isLock() {
        return !lockUsers.isEmpty();
    }

    public Set<User> getLockUsers() {
        return lockUsers;
    }

    @Override
    public Directory getParent() {
        return (Directory) super.getParent();
    }

    @Override
    NodeType getType() {
        return NodeType.FILE;
    }
}
