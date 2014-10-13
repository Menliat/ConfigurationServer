package net.thumbtack.configServer.services;

import net.thumbtack.configServer.thrift.generated.*;
import org.apache.thrift.TException;

import java.util.List;

public class ConfigServiceImpl implements ConfigService.Iface {
    @Override
    public void create(String key) throws DuplicateKeyException, InvalidKeyException, TException {

    }

    @Override
    public void createWithValue(String key, String value) throws DuplicateKeyException, InvalidKeyException, TException {

    }

    @Override
    public void remove(String key) throws UnknownKeyException, InvalidKeyException, TException {

    }

    @Override
    public boolean exists(String key) throws TException {
        return false;
    }

    @Override
    public String getValue(String key) throws UnknownKeyException, InvalidKeyException, TException {
        return "ololo";
    }

    @Override
    public void setValue(String key, String value) throws UnknownKeyException, InvalidKeyException, TException {

    }

    @Override
    public List<String> getChildren(String key) throws UnknownKeyException, InvalidKeyException, TException {
        return null;
    }
}
