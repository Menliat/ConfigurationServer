package net.thumbtack.configServer.server;

import net.thumbtack.configServer.domain.NodeDump;
import net.thumbtack.configServer.serialization.*;
import net.thumbtack.configServer.services.InMemoryConfigService;
import net.thumbtack.configServer.services.LoggingConfigService;
import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.*;
import java.util.Properties;

public class ConsoleServer {
    private static final XLogger LOG = XLoggerFactory.getXLogger(ConsoleServer.class);
    private static String dumpFileName = null;
    private static StreamSerializer<NodeDump> dumpSerializer = null;
    private static Deserializer<InputStream, NodeDump> dumpDeserializer = null;

    public static void main(String[] args) {
        try {
            Properties config = new Properties();
            String propFileName = "server_config.properties";
            InputStream inputStream = ConsoleServer.class.getClassLoader().getResourceAsStream(propFileName);
            config.load(inputStream);

            final InMemoryConfigService configService = new InMemoryConfigService();
            final TServer server = configureServer(config, new LoggingConfigService(configService));

            startServerThread(server);
            configureSerializers(config);
            restoreServerState(configService);
            evaluateCommands(server, configService);
        } catch (TTransportException e) {
            LOG.error("Failed to setup transport", e);
        } catch (IOException e) {
            LOG.catching(e);
        } catch (SerializationException e) {
            LOG.catching(e);
        }
    }

    private static void configureSerializers(Properties config) {
        final String encoding = config.getProperty("serialization.encoding");
        final JsonNodeDumpStreamSerializer serializer = new JsonNodeDumpStreamSerializer(encoding);
        dumpSerializer = serializer;
        dumpDeserializer = serializer;
        dumpFileName = config.getProperty("serialization.dumpFileName");
    }

    private static void startServerThread(final TServer server) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                server.serve();
            }
        });
        thread.start();
    }

    private static void evaluateCommands(TServer server, InMemoryConfigService configService) throws IOException, SerializationException {
        LOG.info("Reading commands");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean shouldReceiveCommands = true;
        while (shouldReceiveCommands) {
            String command = reader.readLine();
            switch (command) {
                case "exit": {
                    saveServerState(configService);
                    stopServer(server);
                    shouldReceiveCommands = false;
                    break;
                }
                case "dump": {
                    saveServerState(configService);
                    break;
                }
                case "restore": {
                    restoreServerState(configService);
                    break;
                }
                default: {
                    LOG.info("Unknown command.");
                    break;
                }

            }
        }
    }

    private static void stopServer(TServer server) {
        LOG.info("Stopping the server");
        server.stop();
        LOG.info("Server stopped");
    }

    private static void saveServerState(InMemoryConfigService configService) throws SerializationException, IOException {
        LOG.info("Saving service state to {}", dumpFileName);
        FileOutputStream outputStream = null;
        try {
            NodeDump dump = configService.getDump();
            outputStream = new FileOutputStream(dumpFileName);
            dumpSerializer.serialize(dump, outputStream);
            LOG.info("Successfully saved");
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static void restoreServerState(InMemoryConfigService configService) throws SerializationException, IOException {
        LOG.info("Restoring service state from {}", dumpFileName);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(dumpFileName);
            NodeDump dump = dumpDeserializer.deserialize(inputStream);
            configService.restore(dump);
            LOG.info("Successfully restored");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static TServer configureServer(Properties config, LoggingConfigService configService) throws TTransportException {
        final int port = Integer.parseInt(config.getProperty("server.port"));
        final int maxThreads = Integer.parseInt(config.getProperty("server.maxThreadsCount", "50"));
        final int minThreads = Integer.parseInt(config.getProperty("server.minThreadsCount", "5"));

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
}
