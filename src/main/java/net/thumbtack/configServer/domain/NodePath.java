package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.InvalidKeyException;
import org.apache.http.annotation.Immutable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Immutable
public class NodePath {
    private Path path;

    public NodePath(String path) throws InvalidKeyException {
        try {
            this.path = Paths.get(path);
        } catch (InvalidPathException ex) {
            throw new InvalidKeyException(String.format("Given string %s is not a valid path.", path));
        }
    }

    private NodePath(Path path) { this.path = path; }

    public String getLastLevel() {
        return path.getName(path.getNameCount() - 1).toString();
    }

    public List<String> getLevels() {
        int capacity = path.getNameCount();
        ArrayList<String> levels = new ArrayList<String>(capacity);
        for (int i = 0; i < capacity; ++i) {
            levels.add(path.getName(i).toString());
        }

        return levels;
    }

    public NodePath getPathExceptLastLevel() throws InvalidKeyException {
        Path parent = path.getParent();
        if (parent == null) {
            return this;
        } else {
            return new NodePath(parent);
        }
    }
}
