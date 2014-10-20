package net.thumbtack.configServer.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.thumbtack.configServer.domain.NodeDump;

import java.io.*;

/**
 * This is a stream serializer/deserializer for NodeDump which converts the object to JSON.
 * It can read NodeDump from InputStream and write it into OutputStream.
 */
public class JsonNodeDumpStreamSerializer implements StreamSerializer<NodeDump>, Deserializer<InputStream, NodeDump> {
    private final String encoding;
    private final Gson gson;

    public JsonNodeDumpStreamSerializer(String encoding) {
        this.encoding = encoding;
        gson = new GsonBuilder()
            .serializeNulls()
        .create();
    }

    @Override
    public NodeDump deserialize(InputStream in) throws SerializationException {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(in, encoding));
            NodeDump dump = gson.fromJson(reader, NodeDump.class);
            reader.close();

            return dump;
        } catch (IOException ex) {
            throw new SerializationException(ex);
        }
    }

    @Override
    public void serialize(NodeDump dump, OutputStream out) throws SerializationException {
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, encoding));
            gson.toJson(dump, NodeDump.class, writer);
            writer.close();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }
}