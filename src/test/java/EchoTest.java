import kvbb.jannis.grpc.RPCClient;
import kvbb.jannis.grpc.RPCServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.logging.Logger;

public class EchoTest {
    private static final int port = 42420;

    private RPCServer server;
    private RPCClient client;

    @Rule
    public Timeout globalTimeout = Timeout.seconds(20);

    @Before
    public void setup() throws IOException {
        server = new RPCServer(port, Logger.getLogger("Server"));
        server.start();
        client = new RPCClient("localhost", port, Logger.getLogger("Client"));
    }

    @Test
    public void requestGreeting() {
        assertEquals(client.greet("Tester"), "Hello Tester");
    }

    @After
    public void shutdown() throws InterruptedException {
        client.shutdown();
        server.stop();
        server.blockUntilShutdown();
    }
}
