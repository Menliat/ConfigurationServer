package net.thumbtack.configServer.serialization;

import net.thumbtack.configServer.domain.NodeDump;

import java.io.OutputStream;

/**
 * Base interface for stream serializers.
 * @param <TInput> type of input
 */
public interface StreamSerializer<TInput> {
    void serialize(TInput input, OutputStream stream) throws SerializationException;
}
