package net.thumbtack.configServer.client;

import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleClient {
    private static final int PORT = 7911;
    private static final Logger log = LoggerFactory.getLogger(ConsoleClient.class);

    public static void main(String[] args) {
        try {
            TTransport transport = new TSocket("localhost", PORT);
            TBinaryProtocol protocol = new TBinaryProtocol(transport);
            ConfigService.Client client = new ConfigService.Client(protocol);
            transport.open();
            log.debug("Query: ololo");
            String value = client.getValue("ololo");
            log.debug("Returned: " + value);

            transport.close();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
    }
}