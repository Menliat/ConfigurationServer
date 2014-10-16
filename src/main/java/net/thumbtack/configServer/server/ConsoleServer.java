package net.thumbtack.configServer.server;

import net.thumbtack.configServer.services.InMemoryConfigService;
import net.thumbtack.configServer.services.LoggingConfigService;
import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class ConsoleServer implements Runnable {
    private static final XLogger LOG = XLoggerFactory.getXLogger(ConsoleServer.class);

    @Override
    public void run() {
        try {
            final XMLConfiguration config = new XMLConfiguration("server_config.xml");
            final LoggingConfigService configService = new LoggingConfigService(new InMemoryConfigService());

            TServer server = configureServer(config, configService);

            server.serve();
        } catch (TTransportException e) {
            LOG.catching(e);
        } catch (ConfigurationException e) {
            LOG.catching(e);
        }
    }

    private TServer configureServer(XMLConfiguration config, LoggingConfigService configService) throws TTransportException {
        final int port = config.getInt("server.port");
        final int maxThreads = config.getInt("server.maxThreadsCount", 50);
        final int minThreads = config.getInt("server.minThreadsCount", 5);

        TServerSocket serverTransport = new TServerSocket(port);
        ConfigService.Processor processor = new ConfigService.Processor(configService);
        TServer server = new TThreadPoolServer(
            new TThreadPoolServer.Args(serverTransport)
                .maxWorkerThreads(maxThreads)
                .minWorkerThreads(minThreads)
                .processor(processor)
        );
        LOG.info("Starting server on port {}", port);

        return server;
    }

    public static void main(String[] args) {
        new Thread(new ConsoleServer()).run();
    }
}
