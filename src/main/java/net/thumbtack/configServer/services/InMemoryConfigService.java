package net.thumbtack.configServer.services;

import net.thumbtack.configServer.domain.Node;
import net.thumbtack.configServer.domain.NodeDump;
import net.thumbtack.configServer.domain.NodePath;
import net.thumbtack.configServer.serialization.StreamSerializer;
import net.thumbtack.configServer.thrift.ConfigService;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Represents a configuration service that stores data in memory.
 */
public class InMemoryConfigService implements ConfigService.Iface {
    private Node root;

    public InMemoryConfigService(Node treeRoot) {
        this.root = treeRoot;
    }

    public InMemoryConfigService() { this(new Node("")); }

    @Override
    public void create(final String key) throws DuplicateKeyException, InvalidKeyException, TException {
        createWithValue(key, "");
    }

    @Override
    public void createWithValue(final String key, final String value) throws DuplicateKeyException, InvalidKeyException, TException {
        NodePath path = new NodePath(key);
        String nodeName = path.getLastLevel();
        NodePath pathToParent = path.getPathExceptLastLevel();
        Node node = new Node(nodeName, value);

        root.insert(pathToParent, node);
    }

    @Override
    public void remove(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        NodePath path = new NodePath(key);
        root.remove(path);
    }

    @Override
    public boolean exists(final String key) throws TException {
        NodePath path = new NodePath(key);

        return root.exists(path);
    }

    @Override
    public String getValue(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        Node found = findNode(key);

        return found.getValue();
    }

    @Override
    public void setValue(final String key, final String value) throws UnknownKeyException, InvalidKeyException, TException {
        Node found = findNode(key);

        found.setValue(value);
    }

    @Override
    public List<String> getChildren(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        Node found = findNode(key);
        List<String> childrenNames = found.getChildrenNames();
        Collections.sort(childrenNames);

        return childrenNames;
    }

    public NodeDump getDump() {
        return root.createDump();
    }

    private Node findNode(String key) throws InvalidKeyException, UnknownKeyException {
        NodePath path = new NodePath(key);
        return root.findNode(path);
    }

    public void restore(NodeDump dump) {
        root.restoreFromDump(dump);
    }
}

