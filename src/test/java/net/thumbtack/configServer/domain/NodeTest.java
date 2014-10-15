package net.thumbtack.configServer.domain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class NodeTest {

    @Test
    public void whenNodeExists_Insert_ShouldThrowDuplicateKeyException() throws InvalidKeyException, UnknownKeyException {
        Node root = new Node("root", new Node("parent", new Node("child", "old value")));
        NodePath pathToInsert = new NodePath("parent");
        Node nodeToInsert = new Node("child", "new value");

        try {
            root.insert(pathToInsert, nodeToInsert);
            fail("Node with given path and name already exists - should throw exception.");
        } catch (DuplicateKeyException ex) {
            Node child = root.getChild("parent").getChild("child");
            assertThat(child.getValue(), is("old value"));
        }
    }

    @Test
    public void whenChildNodeWithGivenNameExists_GetChild_ShouldReturnHim() throws UnknownKeyException {
        Node root = new Node("root", new Node("child"));

        Node child = root.getChild("child");

        assertThat(child, is(notNullValue()));
    }

    @Test (expected = UnknownKeyException.class)
    public void whenChildNodeDoesNotExist_GetChild_ShouldThrowUnknownKeyException() throws UnknownKeyException {
        Node root = new Node("root");

        root.getChild("child");
    }

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
    @Parameters({"child", "a/b/c", "root/root/root", "a/b/a" })
    public void testInsertions(String pathToInsert) throws InvalidKeyException, DuplicateKeyException, UnknownKeyException {
        Node root = new Node("root");
        NodePath path = new NodePath(pathToInsert);
        Node inserted = new Node(path.getLastLevel());
        NodePath pathToParent = path.getPathExceptLastLevel();

        root.insert(pathToParent, inserted);

        Node foundOne = root.findNode(path);
        assertThat(foundOne, is(notNullValue()));
    }
}