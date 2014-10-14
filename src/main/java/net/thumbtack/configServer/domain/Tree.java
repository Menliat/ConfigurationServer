package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.DuplicateKeyException;

public class Tree {
    private Node root;

    public Tree() { root = new Node(""); }

    public void add(NodePath path, Node node) throws DuplicateKeyException {
        root.insert(path, node);
    }
}
