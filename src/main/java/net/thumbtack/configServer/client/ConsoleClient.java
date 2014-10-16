package net.thumbtack.configServer.client;

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

            String value = client.getValue("ololo");
            LOG.debug("Returned: {}", value);
        } catch (TTransportException e) {
            LOG.catching(e);
        } catch (TException e) {
            LOG.catching(e);
        } catch (ConfigurationException e) {
            LOG.catching(e);
        }
        finally {
            if (transport != null) {
                transport.close();
            }

            LOG.info("Listening stopped");
        }
    }

    private static TTransport configureTransport(XMLConfiguration config) {
        final String url = config.getString("client.url");
        final int port = config.getInt("client.port");

        LOG.info("Configured transport to listen {} port {}", url, port);

        return new TSocket(url, port);
    }
}