package kvbb.jannis.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import kvbb.jannis.grpc.service.GreeterService;
import kvbb.jannis.grpc.service.StoreService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPCServer {
    private Server server;
    private final Logger logger;

    public RPCServer(int port, Logger logger) {
        server = ServerBuilder.forPort(port)
                .addService(new GreeterService())
                .addService(new StoreService())
                .build();
        this.logger = logger;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warning("JVM shutting down, stopping...");
            try {
                stop();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Interrupted while waiting for" +
                        "server to stop", e);
            }
        }));
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() throws InterruptedException {
        if(!server.isTerminated() && !server.isShutdown()) {
            logger.info("Stopping server...");
            if(!server.shutdown().awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning("Server not stopped after 5 seconds, " +
                        "stopping forcefully!");
                if(!server.shutdownNow().awaitTermination(10,
                        TimeUnit.SECONDS)) {
                    logger.severe("Failed to stop server!");
                    return;
                }
            }
            logger.info("Server stopped.");
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if(server.isTerminated()) {
            return;
        }
        server.awaitTermination();
    }
}
