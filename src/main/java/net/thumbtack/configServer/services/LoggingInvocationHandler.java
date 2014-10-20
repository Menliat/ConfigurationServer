package net.thumbtack.configServer.services;

import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This object is used for creating a dynamic proxy of type ConfigService.Iface.
 * This proxy will log entry/exit of every method call.
 */
public class LoggingInvocationHandler implements InvocationHandler {
    private static final XLogger LOG = XLoggerFactory.getXLogger(InMemoryConfigService.class);
    private final ConfigService.Iface internalService;

    public LoggingInvocationHandler(ConfigService.Iface internalService) {

        this.internalService = internalService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.info("calling {}({})", method.getName(), StringUtils.join(args, " , "));
        try {
            Object result = method.invoke(internalService, args);
            LOG.info("returned {}", method.getName(), result);
            return result;
        } catch (InvocationTargetException ex) {
            LOG.warn("throwing {}", ex.getTargetException().getClass().getSimpleName());
            throw ex.getTargetException();
        }
    }
}
