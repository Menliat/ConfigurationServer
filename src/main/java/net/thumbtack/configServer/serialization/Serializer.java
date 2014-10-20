package net.thumbtack.configServer.serialization;

import net.thumbtack.configServer.domain.NodeDump;

/**
 * Base interface for serializers that store the entire result in memory.
 * @param <TInput> type of object to be serialized
 * @param <TResult> the result of serialization
 */
public interface Serializer<TInput, TResult> {
    TResult serialize(TInput input);
}
