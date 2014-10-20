package net.thumbtack.configServer.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.thumbtack.configServer.domain.NodeDump;

public class JsonNodeDumpSerializer implements Deserializer<String, NodeDump>, Serializer<NodeDump, String> {
    private final String encoding;
    private final Gson gson;

    public JsonNodeDumpSerializer(String encoding, boolean isPrettyPrinted) {
        this.encoding = encoding;
        GsonBuilder builder = new GsonBuilder()
            .serializeNulls();
        if (isPrettyPrinted) {
            builder.setPrettyPrinting();
        }
        gson = builder.create();
    }

    @Override
    public NodeDump deserialize(String json) throws SerializationException {
        return gson.fromJson(json, NodeDump.class);
    }

    @Override
    public String serialize(NodeDump dump) {
        return gson.toJson(dump, NodeDump.class);
    }
}

