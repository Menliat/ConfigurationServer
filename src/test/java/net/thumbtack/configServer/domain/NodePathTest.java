package net.thumbtack.configServer.domain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@RunWith(JUnitParamsRunner.class)
public class NodePathTest {

    @Test
    @Parameters({"c, a/b/c", ","})
    public void testGetLastLevel(final String expectedLastLevel, final String actualPath) throws InvalidKeyException {
        final NodePath path = new NodePath(actualPath);

        final String lastLevel = path.getLastLevel();

        assertThat(lastLevel, is(expectedLastLevel));
    }


    @Test
    @Parameters(method = "getTestingDataForGetLevels")
    public void testGetLevels(String givenPath, String[] expectedLevels) throws InvalidKeyException {
        final NodePath path = new NodePath(givenPath);

        final Iterable<String> levels = path.getLevels();

        assertThat(levels, contains(expectedLevels));
    }

    private Object getTestingDataForGetLevels() {
        return $(
                $("a/b/c", new String[] {"a", "b", "c" }),
                $("with space/another one", new String[] { "with space", "another one" })
        );
    }

    @Test
    public void testToString() throws InvalidKeyException {
        final NodePath path = new NodePath("a/b/c");

        final String pathAsString = path.toString();

        assertThat(pathAsString, is("a/b/c"));
    }

    @Test
    @Parameters({"a/b/c, a/b", ","})
    public void testGetPathExceptLastLevel(String givenPath, String expectedPath) throws InvalidKeyException {
        final NodePath path = new NodePath(givenPath);

        final NodePath exceptLastLevelPath = path.getPathExceptLastLevel();

        assertThat(exceptLastLevelPath.toString(), is(expectedPath));
    }
}