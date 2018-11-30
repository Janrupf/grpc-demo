package kvbb.jannis.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kvbb.jannis.grpc.exceptions.InvalidCredentialsException;
import kvbb.jannis.grpc.exceptions.InvalidSessionTokenException;
import kvbb.jannis.grpc.exceptions.RPCException;
import kvbb.jannis.grpc.exceptions.DetailedErrorException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RPCClient {
    private final Logger logger;
    private final ManagedChannel channel;
    private GreeterGrpc.GreeterBlockingStub greeterBlockingStub;
    private StoreGrpc.StoreBlockingStub storeBlockingStub;

    public RPCClient(String host, int port, Logger logger) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext().build();
        greeterBlockingStub = GreeterGrpc.newBlockingStub(channel);
        storeBlockingStub = StoreGrpc.newBlockingStub(channel);
        this.logger = logger;
    }

    public void shutdown() throws InterruptedException {
        logger.info("Stopping client...");
        if(!channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)) {
            logger.warning("Client channel not stopped after 5 seconds, " +
                    "stopping forcefully!");
            if(!channel.shutdownNow().awaitTermination(10,
                    TimeUnit.SECONDS)) {
                logger.severe("Failed to shutdown client!");
                return;
            }
        }
        logger.info("Client stopped.");
    }

    public String greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response = greeterBlockingStub.sayHello(request);
        return response.getMessage();
    }

    public Session login(String username, String password) throws RPCException {
        LoginDetails details = LoginDetails.newBuilder()
                .setName(username)
                .setPassword(password)
                .build();
        LoginResponse response = storeBlockingStub.login(details);
        if(response.hasErrorDetails()) {
            if(response.getErrorDetails().getType() == ErrorType.INVALID_CREDENTIALS) {
                throw new InvalidCredentialsException(response.getErrorDetails().getMessage());
            } else {
                throw new DetailedErrorException(response.getErrorDetails());
            }
        }
        return response.getSession();
    }

    public void addItem(Session session, String name, int prize) throws RPCException {
        Item itemToAdd = Item.newBuilder().setName(name).setPrize(prize).build();
        AddItemRequest request = AddItemRequest.newBuilder().setSession(session).setItem(itemToAdd).build();
        AddItemResponse response = storeBlockingStub.addItem(request);
        if(response.hasErrorDetails()) {
            throw makeException(response.getErrorDetails());
        }
    }

    public Item getItem(Session session, String name) throws RPCException {
        GetItemRequest request = GetItemRequest.newBuilder().setSession(session).setItemName(name).build();
        GetItemResponse response = storeBlockingStub.getItem(request);
        if(!response.hasItem()) {
            throw makeException(response.getErrorDetails());
        }
        return response.getItem();
    }

    public void deleteItem(Session session, String name) throws RPCException {
        DeleteItemRequest request = DeleteItemRequest.newBuilder().setSession(session)
                .setItemName(name).build();
        DeleteItemResponse response = storeBlockingStub.deleteItem(request);
        if(response.hasErrorDetails()) {
            throw makeException(response.getErrorDetails());
        }
    }

    public boolean hasItem(Session session, String name) throws RPCException {
        HasItemRequest request = HasItemRequest.newBuilder().setSession(session)
                .setItemName(name).build();
        HasItemResponse response = storeBlockingStub.hasItem(request);
        if(response.hasErrorDetails()) {
            throw makeException(response.getErrorDetails());
        }
        return response.getHasItem();
    }

    public List<Item> getAvailableItems(Session session) throws RPCException{
        AvailableItemsRequest request = AvailableItemsRequest.newBuilder()
                .setSession(session).build();
        AvailableItemsResponse response = storeBlockingStub.getAvailableItems(request);
        if(response.hasErrorDetails()) {
            throw makeException(response.getErrorDetails());
        }
        return response.getItemsList();
    }

    private RPCException makeException(ErrorDetails details) {
        if(details.getType() == ErrorType.INVALID_SESSION_TOKEN) {
            return new InvalidSessionTokenException();
        } else {
            return new DetailedErrorException(details);
        }
    }
}
