package net.thumbtack.configServer.domain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(JUnitParamsRunner.class)
public class NodePathTest {

    @Test
    @Parameters({"c, a/b/c", ","})
    public void testGetLastLevel(String expectedLastLevel, String actualPath) throws InvalidKeyException {
        NodePath path = new NodePath(actualPath);

        String lastLevel = path.getLastLevel();

        assertThat(lastLevel, is(expectedLastLevel));
    }

    @Test
    public void testGetLevels(String e) throws InvalidKeyException {
        NodePath path = new NodePath("a/b/c");

        Iterable<String> levels = path.getLevels();

        assertThat(levels, contains("a", "b", "c"));
    }
}