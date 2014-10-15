package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.thrift.InvalidKeyException;
import org.apache.http.annotation.Immutable;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * NodePath is a path to some node in tree.
 */
@Immutable
public class NodePath {
    private final Path path;

    /**
     * Constructs a path to some node.
     * @param path raw path to some node
     * @throws InvalidKeyException it the given string is not a valid path
     */
    public NodePath(final String path) throws InvalidKeyException {
        try {
            this.path = Paths.get(path);
        } catch (InvalidPathException ex) {
            throw new InvalidKeyException(String.format("Given string %s is not a valid path.", path));
        }
    }

    private NodePath(Path path) { this.path = path; }

    /**
     * @return last level of the path.
     */
    public String getLastLevel() {
        return path.getName(path.getNameCount() - 1).toString();
    }

    /**
     * @return all levels of the path
     */
    public List<String> getLevels() {
        final int capacity = path.getNameCount();
        ArrayList<String> levels = new ArrayList<String>(capacity);
        for (int i = 0; i < capacity; ++i) {
            levels.add(path.getName(i).toString());
        }

        return levels;
    }

    /**
     * @return path of the parent or the same path if there is no parent.
     * @throws InvalidKeyException
     */
    public NodePath getPathExceptLastLevel() throws InvalidKeyException {
        final Path parent = path.getParent();
        if (parent == null) {
            return this;
        } else {
            return new NodePath(parent);
        }
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
