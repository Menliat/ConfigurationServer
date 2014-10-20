package net.thumbtack.configServer.serialization;

import net.thumbtack.configServer.domain.NodeDump;

/**
 * Base interface for deserializers.
 * @param <TInput> type of object from which we can construct result
 * @param <TResult> type of deserialized object
 */
public interface Deserializer<TInput, TResult> {
    TResult deserialize(TInput input) throws SerializationException;
}
