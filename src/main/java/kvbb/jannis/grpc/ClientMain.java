package kvbb.jannis.grpc;

import kvbb.jannis.grpc.exceptions.RPCException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientMain {
    private static final Logger logger = Logger.getLogger("Client");

    public static void main(String[] args) {
        RPCClient client = new RPCClient("localhost", 42420, logger);
        String name = args.length > 0 ? args[0] : "unknown";

        try {
            Session session = client.login("TestUser", "Password");
            client.addItem(session, "TestItem", 20);
            for (Item availableItem : client.getAvailableItems(session)) {
                logger.info("Item: " + availableItem.getName() + " Prize:" + availableItem.getPrize());
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, "Client threw exception", e);
        }
        try {
            client.shutdown();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while waiting for client to stop", e);
        }
    }
}
