package net.thumbtack.configServer.services;

import net.thumbtack.configServer.thrift.ConfigService;
import net.thumbtack.configServer.thrift.DuplicateKeyException;
import net.thumbtack.configServer.thrift.InvalidKeyException;
import net.thumbtack.configServer.thrift.UnknownKeyException;
import org.apache.thrift.TException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

public class LoggingConfigService implements ConfigService.Iface {
    private ConfigService.Iface internalService;
    private static final XLogger LOG = XLoggerFactory.getXLogger(InMemoryConfigService.class);

    public LoggingConfigService(ConfigService.Iface internalService) {
        this.internalService = internalService;
    }

    @Override
    public void create(String key) throws DuplicateKeyException, InvalidKeyException, TException {
        LOG.entry(key);
        internalService.create(key);
        LOG.exit();
    }

    @Override
    public void createWithValue(final String key, final String value) throws DuplicateKeyException, InvalidKeyException, TException {
        LOG.entry(key, value);
        logPossibleExceptions(new ThriftExceptionThrowingAction() {
            @Override
            public void run() throws TException {
                internalService.createWithValue(key, value);
            }
        });
        LOG.exit();
    }

    @Override
    public void remove(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        LOG.entry(key);
        logPossibleExceptions(new ThriftExceptionThrowingAction() {
            @Override
            public void run() throws TException {
                internalService.remove(key);
            }
        });
        LOG.exit();
    }

    @Override
    public boolean exists(String key) throws TException {
        LOG.entry(key);
        boolean exists = internalService.exists(key);
        LOG.exit(exists);
        return exists;
    }

    @Override
    public String getValue(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        LOG.entry(key);
        String value = logPossibleExceptions(new ThriftExceptionThrowingFunction<String>() {
            @Override
            public String call() throws TException {
                return internalService.getValue(key);
            }
        });
        LOG.exit(value);
        return value;
    }

    @Override
    public void setValue(final String key, final String value) throws UnknownKeyException, InvalidKeyException, TException {
        LOG.entry(key, value);
        logPossibleExceptions(new ThriftExceptionThrowingAction() {
            @Override
            public void run() throws TException {
                internalService.setValue(key, value);
            }
        });
        LOG.exit();
    }

    @Override
    public List<String> getChildren(final String key) throws UnknownKeyException, InvalidKeyException, TException {
        LOG.entry(key);
        List<String> children = logPossibleExceptions(new ThriftExceptionThrowingFunction<List<String>>() {
            @Override
            public List<String> call() throws TException {
                return internalService.getChildren(key);
            }
        });
        LOG.exit(children);
        return children;
    }

    private interface ThriftExceptionThrowingFunction<TResult> {
        TResult call() throws TException;
    }

    private interface ThriftExceptionThrowingAction {
        void run() throws TException;
    }

    private void logPossibleExceptions(ThriftExceptionThrowingAction delegate) throws TException {
        try {
            delegate.run();
        } catch (TException ex) {
            rethrowException(ex);
        }
    }

    private <TResult> TResult logPossibleExceptions(ThriftExceptionThrowingFunction<TResult> function) throws TException {
        try {
            TResult result = function.call();
            return result;
        } catch (TException ex) {
            return rethrowException(ex);
        }
    }

    private <TResult> TResult rethrowException(TException ex) throws TException {
        LOG.warn("throwing {}", ex.getClass().getSimpleName());
        throw ex;
    }
}
