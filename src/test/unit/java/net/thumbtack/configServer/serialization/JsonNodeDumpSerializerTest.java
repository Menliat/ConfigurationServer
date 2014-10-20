package net.thumbtack.configServer.serialization;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.thumbtack.configServer.domain.NodeDump;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import static junitparams.JUnitParamsRunner.$;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(JUnitParamsRunner.class)
public class JsonNodeDumpSerializerTest {
    private JsonNodeDumpStreamSerializer streamSerializer;
    private String encoding;
    private JsonNodeDumpSerializer serializer;

    @Before
    public void setUp() {
        encoding = "UTF-8";
        streamSerializer = new JsonNodeDumpStreamSerializer(encoding);
        serializer = new JsonNodeDumpSerializer(encoding, false);
    }

    private Object getTestData() {
        return $(
            $(new NodeDump("root", "value"), "{\"name\":\"root\",\"value\":\"value\",\"children\":[]}"),
            $(
                new NodeDump("root", "value", new NodeDump("child", "value")),
                "{\"name\":\"root\",\"value\":\"value\",\"children\":[{\"name\":\"child\",\"value\":\"value\",\"children\":[]}]}"
            )
        );
    }

    @Test
    @Parameters(method = "getTestData")
    public void testNodeDumpStreamSerialization(NodeDump dump, String expectedJson) throws SerializationException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        streamSerializer.serialize(dump, stream);

        String json = stream.toString();
        assertThat(json, is(expectedJson));
    }

    @Test
    @Parameters(method = "getTestData")
    public void testNodeDumpStreamDeserialization(NodeDump expectedDump, String givenJson) throws SerializationException, UnsupportedEncodingException {
        ByteArrayInputStream stream = new ByteArrayInputStream(givenJson.getBytes(encoding));

        NodeDump actualDump = streamSerializer.deserialize(stream);

        assertThat(actualDump, is(expectedDump));
    }

    @Test
    @Parameters(method = "getTestData")
    public void testNodeDumpSerialization(NodeDump dump, String expectedJson) throws SerializationException {
        String json = serializer.serialize(dump);

        assertThat(json, is(expectedJson));
    }

    @Test
    @Parameters(method = "getTestData")
    public void testNodeDumpDeserialization(NodeDump expectedDump, String givenJson) throws SerializationException {
        NodeDump actualDump = serializer.deserialize(givenJson);

        assertThat(actualDump, is(expectedDump));
    }
}
