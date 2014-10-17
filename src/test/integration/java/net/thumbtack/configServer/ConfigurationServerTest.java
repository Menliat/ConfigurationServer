package net.thumbtack.configServer;

import com.google.common.base.Joiner;
import net.thumbtack.configServer.thrift.ConfigService;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.Configuration;
import java.util.List;

/**
 * This is not a test in a general sense of word - there are no assertions.
 * This class will be used by JMeter to run load tests on server.
 */
@Ignore("JMeter will run it")
public class ConfigurationServerTest {
    private TTransport transport = null;
    private ConfigService.Client client = null;
    private String testingItemKey = "default";
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServerTest.class);

    public ConfigurationServerTest(String testingItemKey) {
        this.testingItemKey = testingItemKey;
    }

    @Before
    public void setUp() throws TTransportException, ConfigurationException {
        final XMLConfiguration config = new XMLConfiguration("test_client_config.xml");

        transport = new TSocket(config.getString("client.url"), config.getInt("client.port"));
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        client = new ConfigService.Client(protocol);

        transport.open();
    }

    @After
    public void tearDown() { transport.close(); }

    @Test
    public void testGetValue() {
        suppressDomainExceptions(new ConfigurationServerCommand("get_value") {
            @Override
            public void execute() throws TException {
                String value = client.getValue(testingItemKey);
                LOG.info("Value for {} is {}", testingItemKey, value);
            }
        });
    }

    @Test
    public void testInsert() {
        suppressDomainExceptions(new ConfigurationServerCommand("insert") {
            @Override
            public void execute() throws TException {
                client.create(testingItemKey);
                LOG.info("Value with key {} created", testingItemKey);
            }
        });
    }

    @Test
    public void testRemove() {
        suppressDomainExceptions(new ConfigurationServerCommand("remove") {
            @Override
            public void execute() throws TException {
                client.remove(testingItemKey);
                LOG.info("Value with key {} removed", testingItemKey);
            }
        });
    }

    @Test
    public void testGetChildren() {
        suppressDomainExceptions(new ConfigurationServerCommand("get_children") {
            @Override
            public void execute() throws TException {
                List<String> children = client.getChildren(testingItemKey);
                LOG.info("Children of {} are {}", testingItemKey, Joiner.on(" , ").join(children));
            }
        });
    }

    @Test
    public void testExists() {
        suppressDomainExceptions(new ConfigurationServerCommand("exists") {
            @Override
            public void execute() throws TException {
                boolean exists = client.exists(testingItemKey);
                LOG.info("Item with key {} exists? {}", testingItemKey, exists ? "YES" : "NO");
            }
        });
    }

    private void suppressDomainExceptions(ConfigurationServerCommand command) {
        try {
            command.execute();
        } catch (TException ex) {
            LOG.info("Caught domain exception of type {} for operation {}", ex.getClass().getSimpleName(), command.getName());
        }
    }

    private abstract class ConfigurationServerCommand {
        private final String name;

        public ConfigurationServerCommand(String name) { this.name = name; }

        public abstract void execute() throws TException;
        String getName() { return this.name; }
    }
}
