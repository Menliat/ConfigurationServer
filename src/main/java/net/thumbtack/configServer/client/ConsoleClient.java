package net.thumbtack.configServer.client;

import com.google.common.base.Function;
import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.transform;

public class ConsoleClient {
    private static final XLogger LOG = XLoggerFactory.getXLogger(ConsoleClient.class);

    public static void main(String[] args) {
        TTransport transport = null;
        try {
            final XMLConfiguration config = new XMLConfiguration("client_config.xml");

            transport = configureTransport(config);
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            ConfigService.Client client = new ConfigService.Client(protocol);

            transport.open();
            LOG.info("Listening started");
            evaluateCommands(System.in, client);

        } catch (Exception e) {
            LOG.catching(e);
        } finally {
            if (transport != null) {
                transport.close();
            }

            LOG.info("Listening stopped");
        }
    }

    private static void evaluateCommands(InputStream stream, ConfigService.Client client) throws TException, IOException, NoSuchMethodException {
        BufferedReader commandsReader = new BufferedReader(new InputStreamReader(stream));
        LOG.info("USAGE: <method name> \"<parameter1>\" \"<parameter2>\"");
        while (true) {
            try {
                String command = commandsReader.readLine();
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }
                String[] arguments = command.split("\\s");
                String methodName = arguments[0];
                List<Object> otherArguments = getMethodArguments(arguments);

                Class[] methodParameters = getClassesOfArguments(otherArguments);
                Method operation = client.getClass().getMethod(methodName, methodParameters);

                Object returned = operation.invoke(client, otherArguments.toArray());
                LOG.info("Returned: {}", returned);

            } catch (NoSuchMethodException ex) {
                LOG.info("Wrong command");
            } catch (InvocationTargetException ex) {
                LOG.info("Catched exception of type {}", ex.getCause().getClass().getSimpleName());
            } catch (Exception e) {
                LOG.catching(e);
            }
        }

    }

    private static List<Object> getMethodArguments(String[] arguments) {
        return transform(Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length)), new Function<String, Object>() {
            @Override
            public Object apply(String s) {
                String transformed = s.trim().replaceAll("\"", "").trim();
                if (transformed.startsWith("l")) {
                    return Long.parseLong(transformed.substring(1));
                }
                return transformed;
            }
        });
    }

    private static Class[] getClassesOfArguments(List<Object> otherArguments) {
        Class[] methodParameters = new Class[otherArguments.size()];
        for (int i = 0; i < otherArguments.size(); i++) {
            if (otherArguments.get(i).getClass() == Long.class) {
                methodParameters[i] = long.class;
            } else {
                methodParameters[i] = otherArguments.get(i).getClass();
            }
        }
        return methodParameters;
    }

    private static TTransport configureTransport(XMLConfiguration config) {
        final String url = config.getString("client.url");
        final int port = config.getInt("client.port");

        LOG.info("Configured transport to listen {} port {}", url, port);

        return new TSocket(url, port);
    }
}