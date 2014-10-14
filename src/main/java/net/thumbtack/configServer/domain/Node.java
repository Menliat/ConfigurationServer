package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.DuplicateKeyException;

import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private String name;
    private ConcurrentHashMap<String, Node> children;
    private String value;

    public Node(String name, String value) {
        this.name = name;
        this.value = value;
        children = new ConcurrentHashMap<String, Node>();
    }

    public Node(String name) { this(name, ""); }

    public String getName() { return name; }

    public void insert(NodePath relativePath, Node node) throws DuplicateKeyException {
        append(relativePath.getLevels().listIterator(), node);
    }

    private void append(ListIterator<String> levels, Node inserted) throws DuplicateKeyException {
        if (levels.hasNext()) {
            String nextLevel = levels.next();
            Node next = children.get(nextLevel);
            if (next == null) {
                int nextIteratorPosition = levels.nextIndex();
                Node newNode = createNotExistingNodes(nextLevel, levels, inserted);
                Node previousChild = children.putIfAbsent(nextLevel, newNode);
                if (previousChild != null) {
                    if (levels.hasNext()) {
                        MoveBack(levels, nextIteratorPosition);
                        previousChild.append(levels, inserted);
                    } else {
                        throw new DuplicateKeyException("Node with given path already exists.");
                    }
                }
            } else {
                next.append(levels, inserted);
            }
        } else {
            throw new DuplicateKeyException("Node with given path already exists.");
        }
    }

    private void MoveBack(ListIterator<String> iterator, int position) {
        while (iterator.nextIndex() != position + 1 || iterator.previousIndex() != position - 1) {
            iterator.previous();
        }
    }

    private Node createNotExistingNodes(String currentLevel, ListIterator<String> notPresentedLevels, Node inserted) {
        if (notPresentedLevels.hasNext()) {
            Node root = new Node(currentLevel);
            Node child = createNotExistingNodes(notPresentedLevels.next(), notPresentedLevels, inserted);
            root.children.put(child.getName(), child);

            return root;
        } else {
            return inserted;
        }
    }

    private Boolean seemsToBeEqual(Node first, Node second) {
        return first == second;
    }
}
