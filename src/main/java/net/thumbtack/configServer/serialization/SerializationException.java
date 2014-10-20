package net.thumbtack.configServer.serialization;

import java.io.UnsupportedEncodingException;

public class SerializationException extends Exception {
    public SerializationException(Throwable ex) {
        super(ex);
    }
}
