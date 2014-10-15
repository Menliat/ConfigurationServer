package net.thumbtack.configServer.domain;

import com.google.common.base.Strings;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;

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

    public Node(final String name, Node... children) {
        this(name, "", children);
    }

    /**
     * Constructs a node with given name and default value "".
     * @param name
     */
    public Node(final String name) { this(name, ""); }

    /**
     * Returns a child of the current node.
     * @param name child name
     * @return child node if presented
     * @throws UnknownKeyException if there is no child with the given name.
     */
    public Node getChild(String name) throws UnknownKeyException {
        Node child = children.get(name);
        if (child == null) {
            throw new UnknownKeyException(String.format("There is no child with key %s", name));
        } else {
            return child;
        }
    }

    /**
     * Finds the node in tree.
     * @param path path to the required node.
     * @return the required node if found
     * @throws UnknownKeyException if there is no node with given path
     */
    public Node findNode(NodePath path) throws UnknownKeyException {
        return find(path.getLevelsIterator());
    }

    public String getValue() {
        return value;
    }

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
        insert(relativePath.getLevelsIterator(), node);
    }

    private Node find(ListIterator<String> levels) throws UnknownKeyException {
        if (levels.hasNext()) {
            final String currentLevel = levels.next();
            Node next = getChild(currentLevel);

            return next.find(levels);
        } else {
            return this;
        }
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
        Node previousNodeOnLevel = children.putIfAbsent(newNode.name, newNode);
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
            final Node child = createNotExistingNodes(notPresentedLevels.next(), notPresentedLevels, inserted);

            return new Node(currentLevel, child);
        } else {
            if (Strings.isNullOrEmpty(currentLevel)) {
                return inserted;
            } else {
                return new Node(currentLevel, inserted);
            }
        }
    }
}
