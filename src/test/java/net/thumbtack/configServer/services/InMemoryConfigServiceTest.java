package net.thumbtack.configServer.services;

import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class InMemoryConfigServiceTest {

    private InMemoryConfigService service;

    @Before
    public void SetUp() {
        service = new InMemoryConfigService();
    }

    @Test
    public void WhenNodeAlreadyExists_Create_ShouldThrowDuplicateKeyException() throws TException {
        service.create("key");
        try {
            service.create("key");
            fail("Expected an DuplicateKeyException to be thrown");
        } catch (DuplicateKeyException ex) { }
    }

    @Test (expected = InvalidKeyException.class)
    public void WhenKeyContainsLevelWithEmptyName_Create_ShouldThrowInvalidKeyException() throws TException {
        service.create("key////");
    }

    @Test
    public void WhenValueIsNotSpecified_Create_ShouldUseEmptyStringAsValue() throws TException {
        service.create("key");

        String value = service.getValue("key");

        assertThat(value, is(""));
    }

    @Test
    public void WhenValueIsSpecified_Create_ShouldUseEmptyStringAsValue() throws TException {
        service.createWithValue("key", "value");

        String value = service.getValue("key");

        assertThat(value, is("value"));
    }

    @Test
    public void WhenNodeIsCreated_Exists_ShouldReturnTrue() throws TException {
        service.create("a/b/c");

        Boolean exists = service.exists("a/b/c");

        assertThat(exists, is(true));
    }

    @Test
    public void WhenNodeIsNotCreated_Exists_ShouldReturnFalse() throws TException {
        Boolean exists = service.exists("a/b/c");

        assertThat(exists, is(false));
    }

    @Test(expected = UnknownKeyException.class)
    public void WhenNodeIsNotCreated_Remove_ShouldThrowUnknownKeyException() throws TException {
        service.remove("key");
    }

    @Test
    public void WhenNodeIsCreated_Remove_ShouldMakeItNonExisting() throws TException {
        service.create("key");

        service.remove("key");
        Boolean exists = service.exists("key");

        assertThat(exists, is(false));
    }

    @Test
    public void WhenParentNodeGiven_Remove_ShouldRemoveAllChildNodes() throws TException {
        service.create("parent/child1/child2");
        service.create("parent/child3/child4");

        service.remove("parent");
        Boolean childNodesExists = service.exists("child1") && service.exists("child2") && service.exists("child3") && service.exists("child4");

        assertThat(childNodesExists, is(false));
    }

    @Test
    public void WhenNodeExists_GetValue_ShouldReturnItsValue() throws TException {
        service.createWithValue("key", "value");

        String value = service.getValue("key");

        assertThat(value, is("value"));
    }

    @Test (expected = UnknownKeyException.class)
    public void WhenNodeDoesNotExist_GetValue_ShouldThrowUnknownKeyException() throws TException {
        service.getValue("key");
    }

    @Test (expected = UnknownKeyException.class)
    public void WhenNodeDoesNotExist_SetValue_ShouldThrowUnknownKeyException() throws TException {
        service.setValue("key", "value");
    }

    @Test
    public void WhenNodeExists_SetValue_ShouldChangeHisValue() throws TException {
        service.createWithValue("key", "oldValue");

        service.setValue("key", "newValue");
        String actualValue = service.getValue("key");

        assertThat(actualValue, is("newValue"));
    }

    @Test
    public void WhenNodeHasChildren_GetChildren_ShouldReturnThemOrderedByKey() throws TException {
        service.create("parent/child2");
        service.create("parent/child1");
        service.create("parent/child3");

        List<String> children = service.getChildren("parent");

        assertThat(children, is(Arrays.asList("child1", "child2", "child3")));
    }

    @Test (expected = UnknownKeyException.class)
    public void WhenNodeIsNotExists_GetChildren_ShouldThrowUnknownKeyException() throws TException {
        service.getChildren("parent");
    }

    @Test
    public void WhenNodeHasNoChildren_GetChildren_ShouldReturnEmptyArray() throws TException {
        service.create("parent");

        List<String> children = service.getChildren("parent");

        assertThat(children, is(empty()));
    }
}
