import kvbb.jannis.grpc.Item;
import kvbb.jannis.grpc.RPCClient;
import kvbb.jannis.grpc.RPCServer;
import kvbb.jannis.grpc.Session;
import kvbb.jannis.grpc.exceptions.DetailedErrorException;
import kvbb.jannis.grpc.exceptions.InvalidCredentialsException;
import kvbb.jannis.grpc.exceptions.RPCException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class StoreTest {
    private static final int port = 42420;

    private RPCServer server;
    private RPCClient client;

    private Session login() throws RPCException {
        return client.login("TestUser", "Password");
    }

    @Rule
    public Timeout globalTimeout = Timeout.seconds(20);

    @Before
    public void setup() throws IOException {
        server = new RPCServer(port, Logger.getLogger("Server"));
        server.start();
        client = new RPCClient("localhost", port, Logger.getLogger("Client"));
    }

    @Test
    public void correctLogin() throws RPCException {
        assertNotNull(login().getSessionToken());
    }

    @Test(expected = InvalidCredentialsException.class)
    public void wrongLogin() throws RPCException {
        client.login("SomeUser", "Password");
    }

    @Test
    public void addItem() throws RPCException {
        Session session = login();
        client.addItem(session, "TestItem", 20);
    }

    @Test(expected = DetailedErrorException.class)
    public void doubleAddItem() throws RPCException {
        Session session = login();
        client.addItem(session, "TestItem", 20);
        client.addItem(session, "TestItem", 20);
    }

    @Test
    public void addAndGetItem() throws RPCException {
        Session session = login();
        client.addItem(session, "TestItem", 20);
        Item item = client.getItem(session, "TestItem");
        assertEquals(item.getName(), "TestItem");
        assertEquals(item.getPrize(), 20);
    }

    @Test(expected = DetailedErrorException.class)
    public void getInvalidItem() throws RPCException {
        Session session = login();
        client.getItem(session, "TestItem");
    }

    @Test
    public void hasItem() throws RPCException {
        Session session = login();
        client.addItem(session, "TestItem", 20);
        assertTrue(client.hasItem(session, "TestItem"));
        assertFalse(client.hasItem(session, "NonexistentItem"));
    }

    @Test
    public void deleteItem() throws RPCException {
        Session session = login();
        client.addItem(session, "TestItem", 20);
        assertTrue(client.hasItem(session, "TestItem"));
        client.deleteItem(session, "TestItem");
        assertFalse(client.hasItem(session, "TestItem"));
    }

    @Test(expected = DetailedErrorException.class)
    public void deleteInvalidItem() throws RPCException {
        Session session = login();
        client.deleteItem(session, "TestItem");
    }

    @Test
    public void availableItems() throws RPCException {
        Session session = login();
        assertEquals(client.getAvailableItems(session).size(), 0);

        List<Item> requiredItems = Collections.unmodifiableList(Arrays.asList(
                Item.newBuilder().setName("Item1").setPrize(20).build(),
                Item.newBuilder().setName("Item2").setPrize(40).build()
        ));

        for(Item item : requiredItems) {
            client.addItem(session, item.getName(), item.getPrize());
        }

        assertEquals(client.getAvailableItems(session), requiredItems);
    }

    @After
    public void shutdown() throws InterruptedException {
        client.shutdown();
        server.stop();
        server.blockUntilShutdown();
    }

}
