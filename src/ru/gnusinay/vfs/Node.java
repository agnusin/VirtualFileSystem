package ru.gnusinay.vfs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** Класс описывает элементы файловой системы и
 * хранит их основные характеристики
 */
public abstract class Node {
    private String name;
    private Node parent;
    private ReentrantLock lock;

    Node (String name, Node  parent) {
        this.name = name;
        this.parent = parent;
        this.lock = new ReentrantLock();
    }

    public String getName() {
        return name;
    }


    public Node getParent() {
        return parent;
    }

    public void setParent(Node  parent) {
        this.parent = parent;
    }

    public boolean isLockedNode() throws InterruptedException {
        return lock.isLocked();
    }

    public boolean lockNode() throws InterruptedException {
        return lock.tryLock(1000, TimeUnit.MILLISECONDS);
    }

    public void unlockNode() throws InterruptedException {
        lock.unlock();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getName());
        Node node = this;
        while (node.getParent() != null) {
            node = node.getParent();
            builder.insert(0, '\\');
            builder.insert(0, node.getName());
        }
        return builder.toString();
    }

    abstract NodeType getType();
}
