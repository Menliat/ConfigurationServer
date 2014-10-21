package net.thumbtack.configServer.services;

import net.thumbtack.configServer.domain.Node;
import net.thumbtack.configServer.domain.NodeDump;
import net.thumbtack.configServer.domain.NodePath;
import net.thumbtack.configServer.domain.Scheduler;
import net.thumbtack.configServer.thrift.*;
import org.apache.thrift.TException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Represents a configuration service that stores data in memory.
 */
public class InMemoryConfigService implements ConfigService.Iface {
    private Node root;
    private Scheduler scheduler = null;

    public InMemoryConfigService(Node treeRoot) {
        this.root = treeRoot;
        this.scheduler = new Scheduler();
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
    public void createTemporaryWithValue(String key, String value, long msTimeout) throws DuplicateKeyException, InvalidKeyException, InvalidTimeoutException, TException {
        createWithValue(key, value);
        scheduleItemRemoving(key, msTimeout);
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
        scheduler.reschedule(key);
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

    public void restore(NodeDump dump) {
        root.restoreFromDump(dump);
    }

    private void scheduleItemRemoving(String key, long msTimeout) throws InvalidTimeoutException {
        scheduler.schedule(key, new RemoveItemWithKeyTask(key), msTimeout);
    }

    private Node findNode(String key) throws InvalidKeyException, UnknownKeyException {
        NodePath path = new NodePath(key);
        return root.findNode(path);
    }

    private class RemoveItemWithKeyTask implements Runnable {
        private String key = null;

        public RemoveItemWithKeyTask(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            try {
                remove(key);
            } catch (TException e) {
                // other threads can already remove the given item.
            }
        }
    }
}

