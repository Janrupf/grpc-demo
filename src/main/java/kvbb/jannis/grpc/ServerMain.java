package kvbb.jannis.grpc;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain {
    private static final Logger logger = Logger.getLogger("Server");

    public static void main(String[] args) {
        logger.info("Starting server...");
        RPCServer server = new RPCServer(42420, logger);
        try {
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server threw exception while starting", e);
            System.exit(1);
        }

        try {
            new Scanner(System.in).nextLine();
            server.stop();
            System.exit(0);
        } catch (NoSuchElementException e) {
            logger.log(Level.WARNING, "Failed to read from stdin, " +
                    "process has to be killed for shutdown", e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while waiting for server to stop", e);
            System.exit(1);
        }

        try {
            server.blockUntilShutdown();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while waiting for server to stop", e);
            System.exit(1);
        }
        System.exit(0);
    }
}
