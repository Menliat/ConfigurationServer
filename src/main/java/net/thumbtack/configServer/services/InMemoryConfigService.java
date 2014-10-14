package net.thumbtack.configServer.services;

import net.thumbtack.configServer.domain.Node;
import net.thumbtack.configServer.domain.NodePath;
import net.thumbtack.configServer.domain.Tree;
import net.thumbtack.configServer.thrift.ConfigService;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.apache.thrift.TException;

import java.util.List;

public class InMemoryConfigService implements ConfigService.Iface {
    private Tree tree;

    public InMemoryConfigService(Tree tree) {
        this.tree = tree;
    }

    public InMemoryConfigService() { this(new Tree()); }

    @Override
    public void create(String key) throws DuplicateKeyException, InvalidKeyException, TException {
        createWithValue(key, "");
    }

    @Override
    public void createWithValue(String key, String value) throws DuplicateKeyException, InvalidKeyException, TException {
        NodePath path = new NodePath(key);
        String nodeName = path.getLastLevel();
        NodePath pathToParent = path.getPathExceptLastLevel();
        Node node = new Node(nodeName, value);

        tree.add(pathToParent, node);
    }

    @Override
    public void remove(String key) throws UnknownKeyException, InvalidKeyException, TException {

    }

    @Override
    public boolean exists(String key) throws TException {
        return false;
    }

    @Override
    public String getValue(String key) throws UnknownKeyException, InvalidKeyException, TException {
        return "ololo";
    }

    @Override
    public void setValue(String key, String value) throws UnknownKeyException, InvalidKeyException, TException {

    }

    @Override
    public List<String> getChildren(String key) throws UnknownKeyException, InvalidKeyException, TException {
        return null;
    }
}

