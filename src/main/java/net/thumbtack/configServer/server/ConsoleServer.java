package net.thumbtack.configServer.server;

import net.thumbtack.configServer.services.InMemoryConfigService;
import net.thumbtack.configServer.services.LoggingConfigService;
import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleServer implements Runnable {
    private static final int PORT = 7911;
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleServer.class);

    @Override
    public void run() {
        try {
            TServerSocket serverTransport = new TServerSocket(PORT);
            ConfigService.Processor processor = new ConfigService.Processor(new LoggingConfigService(new InMemoryConfigService()));
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            LOG.info("Starting server on port {}", PORT);

            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new ConsoleServer()).run();
    }
}
