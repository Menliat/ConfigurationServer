package net.thumbtack.configServer.services;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.InvalidTimeoutException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(JUnitParamsRunner.class)
public class InMemoryConfigServiceTest {

    private InMemoryConfigService service;

    @Before
    public void setUp() {
        service = new InMemoryConfigService();
    }

    @Test
    public void whenNodeAlreadyExists_Create_ShouldThrowDuplicateKeyException() throws TException {
        service.create("key");
        try {
            service.create("key");
            fail("Expected an DuplicateKeyException to be thrown");
        } catch (DuplicateKeyException ex) { }
    }

    @Test
    public void whenValueIsNotSpecified_Create_ShouldUseEmptyStringAsValue() throws TException {
        service.create("key");

        final String value = service.getValue("key");

        assertThat(value, is(""));
    }

    @Test
    public void whenPathHasSlashAtStart_Create_ShouldCreateTheRootChild() throws TException {
        service.create("/key");

        checkExistence("key", true);
    }

    @Test
    public void whenValueIsSpecified_Create_ShouldUseThisValue() throws TException {
        service.createWithValue("key", "value");

        final String value = service.getValue("key");

        assertThat(value, is("value"));
    }

    @Test
    public void whenNodeIsCreated_Exists_ShouldReturnTrue() throws TException {
        service.create("a/b/c");

        checkExistence("a/b/c", true);
    }

    @Test
    public void whenNodeIsNotCreated_Exists_ShouldReturnFalse() throws TException {
        checkExistence("a/b/c", false);
    }

    @Test(expected = UnknownKeyException.class)
    public void whenNodeIsNotCreated_Remove_ShouldThrowUnknownKeyException() throws TException {
        service.remove("key");
    }

    @Test
    public void whenNodeIsCreated_Remove_ShouldMakeItNonExisting() throws TException {
        service.create("key");

        service.remove("key");

        checkExistence("key", false);
    }

    @Test
    public void whenParentNodeGiven_Remove_ShouldRemoveAllChildNodes() throws TException {
        service.create("parent/child1/child2");
        service.create("parent/child3/child4");

        service.remove("parent");
        final boolean childNodesExists = service.exists("child1") && service.exists("child2")
                                      && service.exists("child3") && service.exists("child4");

        assertThat(childNodesExists, is(false));
    }

    @Test
    public void whenNodeExists_GetValue_ShouldReturnItsValue() throws TException {
        service.createWithValue("key", "value");

        final String value = service.getValue("key");

        assertThat(value, is("value"));
    }

    @Test (expected = UnknownKeyException.class)
    public void whenNodeDoesNotExist_GetValue_ShouldThrowUnknownKeyException() throws TException {
        service.getValue("key");
    }

    @Test (expected = UnknownKeyException.class)
    public void whenNodeDoesNotExist_SetValue_ShouldThrowUnknownKeyException() throws TException {
        service.setValue("key", "value");
    }

    @Test
    public void whenNodeExists_SetValue_ShouldChangeHisValue() throws TException {
        service.createWithValue("key", "oldValue");

        service.setValue("key", "newValue");
        final String actualValue = service.getValue("key");

        assertThat(actualValue, is("newValue"));
    }

    @Test
    public void whenNodeHasChildren_GetChildren_ShouldReturnThemOrderedByKey() throws TException {
        service.create("parent/child2");
        service.create("parent/child1");
        service.create("parent/child3");

        final List<String> children = service.getChildren("parent");

        assertThat(children, is(Arrays.asList("child1", "child2", "child3")));
    }

    @Test (expected = UnknownKeyException.class)
    public void whenNodeIsNotExists_GetChildren_ShouldThrowUnknownKeyException() throws TException {
        service.getChildren("parent");
    }

    @Test
    public void whenNodeHasNoChildren_GetChildren_ShouldReturnEmptyArray() throws TException {
        service.create("parent");

        final List<String> children = service.getChildren("parent");

        assertThat(children, is(empty()));
    }

    @Test
    @Parameters({"10, 5, true", "5, 10, false"})
    public void testCreateTemporaryWithValue(long timeout, long timeToSleep, boolean expectedExistence) throws TException, InterruptedException {
        service.createTemporaryWithValue("key", "value", timeout);

        Thread.sleep(timeToSleep);

        checkExistence("key", expectedExistence);
    }

    @Test
    public void whenTemporaryValueIsCreated_SetValue_ShouldProlongItsLifetime() throws TException, InterruptedException {
        service.createTemporaryWithValue("key", "value", 10);

        Thread.sleep(5);
        service.setValue("key", "new value");

        Thread.sleep(6);
        checkExistence("key", true);
    }

    @Test
    public void whenTemporaryValueIsCreated_AndTimeoutIsExpiredAfterSetValue_TheNodeShouldBeRemoved() throws TException, InterruptedException {
        service.createTemporaryWithValue("key", "value", 5);

        service.setValue("key", "new value");

        Thread.sleep(11);
        checkExistence("key", false);
    }

    @Test(expected = InvalidTimeoutException.class)
    public void testCreateTemporaryWithValue_whenTimeoutIsInvalid() throws TException {
        service.createTemporaryWithValue("key", "value", -10);
    }

    private void checkExistence(String key, boolean expectedExistence) throws TException {
        boolean exists = service.exists(key);
        assertThat(exists, is(expectedExistence));
    }
}
