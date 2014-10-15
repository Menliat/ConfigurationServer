package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.DuplicateKeyException;

import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Node is a class representing a tree node.
 * Every tree node can have name and value. The default value is "".
 * This class is completely thread-safe, see method documentation for more details.
 */
public class Node {
    private final String name;
    private ConcurrentHashMap<String, Node> children;
    private final String value;

    public Node(final String name, final String value) {
        this.name = name;
        this.value = value;
        children = new ConcurrentHashMap<String, Node>();
    }

    public Node(final String name, final String value, Node... children) {
        this(name, value);
        for (Node child : children) {
            this.children.put(child.name, child);
        }
    }

    /**
     * Constructs a node with given name and default value "".
     * @param name
     */
    public Node(final String name) { this(name, ""); }

    /**
     * Inserts the given node to the tree using the given path.
     * If parent nodes in the hierarchy does not exist, it will create them.
     * This method is thread-safe. It is possible to insert the node into some subtree
     * while other thread can delete this subtree entirely.
     * @param relativePath path where to insert the given node. This path is relative to the current node.
     * @param node node to insert
     * @throws DuplicateKeyException if tree already has a node with the same path and name
     */
    public void insert(final NodePath relativePath, final Node node) throws DuplicateKeyException {
        insert(relativePath.getLevels().listIterator(), node);
    }

    private void insert(ListIterator<String> levels, final Node inserted) throws DuplicateKeyException {
        ensureCurrentNodeIsNotInsertedOne(levels);
        final String currentLevel = levels.next();
        Node next = children.get(currentLevel);
        if (next == null) {
            InsertIntoCurrentPosition(currentLevel, levels, inserted);
        } else {
            next.insert(levels, inserted);
        }
    }

    private void InsertIntoCurrentPosition(String currentLevel, ListIterator<String> levels, Node inserted) throws DuplicateKeyException {
        final int nextIteratorPosition = levels.nextIndex();
        Node newNode = createNotExistingNodes(currentLevel, levels, inserted);
        Node previousNodeOnLevel = children.putIfAbsent(currentLevel, newNode);
        if (previousNodeOnLevel != null) {
            // other thread may already insert some nodes in hierarchy
            ensureCurrentNodeIsNotInsertedOne(levels);
            MoveBack(levels, nextIteratorPosition);
            previousNodeOnLevel.insert(levels, inserted);
        }
    }

    private void ensureCurrentNodeIsNotInsertedOne(ListIterator<String> levels) throws DuplicateKeyException {
        if (!levels.hasNext()) {
            throw new DuplicateKeyException("Node with given path already exists.");
        }
    }

    private void MoveBack(ListIterator<String> iterator, final int position) {
        while (iterator.nextIndex() != position + 1 || iterator.previousIndex() != position - 1) {
            iterator.previous();
        }
    }

    private Node createNotExistingNodes(final String currentLevel, ListIterator<String> notPresentedLevels, final Node inserted) {
        if (notPresentedLevels.hasNext()) {
            Node root = new Node(currentLevel);
            final Node child = createNotExistingNodes(notPresentedLevels.next(), notPresentedLevels, inserted);
            root.children.put(child.name, child);

            return root;
        } else {
            return inserted;
        }
    }
}
