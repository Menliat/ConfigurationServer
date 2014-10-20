package net.thumbtack.configServer.domain;

import net.thumbtack.configServer.serialization.SerializationException;
import org.apache.commons.collections.CollectionUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * NodeDump is a structure that allows a node to be restored.
 */
public class NodeDump {
    private final String name;
    private final String value;
    private final Collection<NodeDump> children;

    public NodeDump(final String name, final String value, final Collection<NodeDump> children) {
        this.name = name;
        this.value = value;
        this.children = children;
    }

    public NodeDump(final String name, final String value) {
        this(name, value, new ArrayList<NodeDump>());
    }

    public NodeDump(final String name, final String value, NodeDump... dumps) {
        this(name, value, Arrays.asList(dumps));
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public Collection<NodeDump> getChildren() { return children; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeDump nodeDump = (NodeDump) o;

        return CollectionUtils.isEqualCollection(children, nodeDump.children) &&
               name.equals(nodeDump.name) &&
               value.equals(nodeDump.value);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + children.hashCode();

        return result;
    }
}
