package net.thumbtack.configServer.domain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class NodeTest {

    @Test
    public void whenNodeWithGivenPathExists_FindNode_ShouldReturnHim() throws UnknownKeyException, InvalidKeyException {
        Node root = new Node("root", new Node("parent", new Node("child")));

        Node child = root.findNode(new NodePath("parent/child"));

        assertThat(child, is(notNullValue()));
    }

    @Test (expected = UnknownKeyException.class)
    public void whenNodeWithGivenPathDoesNotExist_FindNode_ShouldThrowUnknownKeyException() throws UnknownKeyException, InvalidKeyException {
        Node root = new Node("root", new Node("child"));

        root.findNode(new NodePath("root/not_existing_one/another_one"));
    }

    @Test
    public void whenPathIsEmpty_FindNode_ShouldReturnTheRoot() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root");

        Node found = root.findNode(new NodePath(""));

        assertThat(found, is(root));
    }

    @Test
    @Parameters({"child", "a/b/c", "root/root/root", "a/b/a" })
    public void testInsertions(String pathToInsert) throws InvalidKeyException, DuplicateKeyException, UnknownKeyException {
        Node root = new Node("root");
        NodePath path = new NodePath(pathToInsert);
        Node inserted = new Node(path.getLastLevel());
        NodePath pathToParent = path.getPathExceptLastLevel();

        root.insert(pathToParent, inserted);

        assertNodesExistence(true, root, pathToInsert);
    }

    @Test
    public void whenNodeExists_Insert_ShouldThrowDuplicateKeyException() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root", new Node("parent", new Node("child", "old value")));
        NodePath pathToInsert = new NodePath("parent");
        Node nodeToInsert = new Node("child", "new value");

        try {
            root.insert(pathToInsert, nodeToInsert);
            fail("Node with given path and name already exists - should throw exception.");
        } catch (DuplicateKeyException ex) {
            Node child = root.findNode(new NodePath("parent/child"));
            assertThat(child.getValue(), is("old value"));
        }
    }

    @Test
    public void testInsertionIntoSubtree() throws InvalidKeyException, DuplicateKeyException {
        Node root = new Node("root", new Node("subtree", new Node("child")));
        Node inserted = new Node("child2");

        root.insert(new NodePath("subtree"), inserted);

        assertNodesExistence(true, root, "subtree/child2");
    }

    @Test
    public void whenLeafGiven_Remove_ShouldMakeHimNotExisting() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root", new Node("child"));

        root.remove(new NodePath("child"));

        assertNodesExistence(false, root, "child");
    }

    @Test
    public void whenPathToSubtreeGiven_Remove_ShouldDeleteAllNodesInTheGivenTree() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root", new Node("subtree-root", new Node("child1"), new Node("child2")));

        root.remove(new NodePath("subtree-root"));

        assertNodesExistence(false, root, "subtree-root", "subtree-root/child1", "subtree-root/child2");
    }

    @Test(expected = UnknownKeyException.class)
    public void whenRemovingNodeDoesNotExist_Remove_ShouldThrowUnknownKeyException() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root", new Node("subtree"));

        root.remove(new NodePath("subtree/child"));
    }

    @Test
    public void whenNodeExists_Exists_ShouldReturnTrue() throws InvalidKeyException {
        Node root = new Node("root", new Node("parent", new Node("child")));

        assertNodesExistence(true, root, "parent/child");
    }

    @Test
    public void whenNodeDoesNotExists_Exists_ShouldReturnFalse() throws InvalidKeyException {
        Node root = new Node("root", new Node("parent"));

        assertNodesExistence(false, root, "parent/child");
    }

    @Test
    public void whenPathPointsToRoot_Exists_ShouldReturnTrue() throws InvalidKeyException {
        Node root = new Node("root");

        assertNodesExistence(true, root, "");
    }

    @Test
    public void testGetChildrenNames() {
        Node node = new Node("root", new Node("child1"), new Node("child3"), new Node("child2"));

        List<String> names = node.getChildrenNames();

        assertThat(names, containsInAnyOrder("child1", "child2", "child3"));
    }

    @Test
    public void testCreateDump() {
        Node node = new Node("root", "value", new Node("child1"), new Node("child2"));

        NodeDump dump = node.createDump();

        assertThat(dump.getName(), is("root"));
        assertThat(dump.getValue(), is("value"));
        assertThat(dump.getChildren().size(), is(2));
    }

    @Test
    public void testRestoreFromDump() {
        Node node = new Node("some root", "old value", new Node("child1"));
        NodeDump dump = new NodeDump("root", "value",
            new NodeDump("child1", "value"),
            new NodeDump("child2", "value")
        );

        node.restoreFromDump(dump);

        assertThat(node.getValue(), is("value"));
        assertThat(node.getName(), is("root"));
        assertThat(node.getChildrenNames(), containsInAnyOrder("child1", "child2"));
    }

    private void assertNodesExistence(boolean expected, Node root, String... paths) throws InvalidKeyException {
        for (String path : paths) {
            boolean exists = root.exists(new NodePath(path));
            assertThat(exists, is(expected));
        }
    }
}