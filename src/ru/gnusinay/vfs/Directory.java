package ru.gnusinay.vfs;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;


/** Класс описывающий директории в файловой системе. Хранит
 * список всех поддиректорий и файлов в директории в
 * алфавитном порядке
 */
public class Directory extends Node {
    private NavigableSet<Node> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new ConcurrentSkipListSet<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
    }

    public boolean addChild(Node node) {
        return children.add(node);
    }

    public boolean removeChild(Node node) {
        return children.remove(node);
    }

    public boolean hasChildrenDirectory() {
        for (Node node : children) {
            if (node.getType() == NodeType.DIRECTORY) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public NavigableSet<Node> getChildren() {
        return children;
    }

    public boolean isRoot() {
        return getParent() == null;
    }

    @Override
    public Directory getParent() {
        return (Directory) super.getParent();
    }

    @Override
    NodeType getType() {
        return NodeType.DIRECTORY;
    }
}
