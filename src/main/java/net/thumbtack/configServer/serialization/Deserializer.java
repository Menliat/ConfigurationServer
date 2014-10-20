package net.thumbtack.configServer.serialization;

import net.thumbtack.configServer.domain.NodeDump;

/**
 * Base interface for deserializers.
 * @param <TInput> type of object from which we can construct result
 * @param <TResult> type of deserialized object
 */
public interface Deserializer<TInput, TResult> {
    /**
     * This method reads the input and constructs the result.
     * @param input input that is sufficient to construct the result
     * @return the object deserialized
     * @throws SerializationException if any error during deserialization occurs
     */
    TResult deserialize(TInput input) throws SerializationException;
}
