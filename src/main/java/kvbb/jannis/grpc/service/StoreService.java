package kvbb.jannis.grpc.service;

import io.grpc.stub.StreamObserver;
import kvbb.jannis.grpc.*;

import java.util.*;

public class StoreService extends StoreGrpc.StoreImplBase {
    private static final ErrorDetails INVALID_SESSION_DETAILS = ErrorDetails.newBuilder()
            .setMessage("Invalid session token").build();

    private static final Map<String, String> logins;
    static {
        logins = new HashMap<>();
        logins.put("TestUser", "Password");
    }

    private Map<String, Item> items;
    private List<String> validSessionTokens;

    public StoreService() {
        items = new HashMap<>();
        validSessionTokens = new ArrayList<>();
    }

    @Override
    public void login(LoginDetails request, StreamObserver<LoginResponse> responseObserver) {
        if(logins.containsKey(request.getName()) && logins.get(request.getName()).equals(request.getPassword())) {
            String sessionToken = generateSessionToken();
            validSessionTokens.add(sessionToken);
            Session session = Session.newBuilder().setSessionToken(sessionToken).build();
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setSession(session)
                    .build());
        } else {
            responseObserver.onNext(
                    LoginResponse.newBuilder()
                        .setErrorDetails(ErrorDetails.newBuilder()
                                    .setType(ErrorType.INVALID_CREDENTIALS)
                                    .setMessage("Username or password invalid")
                                    .build()
                    ).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void addItem(AddItemRequest request, StreamObserver<AddItemResponse> responseObserver) {
        if(!validSessionTokens.contains(request.getSession().getSessionToken())) {
            responseObserver.onNext(
                    AddItemResponse.newBuilder().setErrorDetails(INVALID_SESSION_DETAILS).build()
            );
        } else {
            if(items.containsKey(request.getItem().getName())) {
                responseObserver.onNext(
                        AddItemResponse.newBuilder()
                                .setErrorDetails(
                                        ErrorDetails.newBuilder()
                                                .setType(ErrorType.ITEM_EXISTS_ALREADY)
                                                .setMessage("Item exists already")
                                                .build()
                                ).build()
                );
            } else {
                items.put(request.getItem().getName(), request.getItem());
                responseObserver.onNext(
                        AddItemResponse.newBuilder()
                                .build()
                );
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getItem(GetItemRequest request, StreamObserver<GetItemResponse> responseObserver) {
        if(!validSessionTokens.contains(request.getSession().getSessionToken())) {
            responseObserver.onNext(
                    GetItemResponse.newBuilder().setErrorDetails(INVALID_SESSION_DETAILS).build()
            );
        } else {
            if(!items.containsKey(request.getItemName())) {
                responseObserver.onNext(GetItemResponse.newBuilder()
                        .setErrorDetails(
                            ErrorDetails.newBuilder()
                                    .setType(ErrorType.ITEM_NOT_FOUND)
                                    .setMessage("Item does not exist")
                                    .build()
                        ).build());
            } else {
                responseObserver.onNext(GetItemResponse.newBuilder()
                        .setItem(items.get(request.getItemName()))
                        .build());
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteItem(DeleteItemRequest request, StreamObserver<DeleteItemResponse> responseObserver) {
        if(!validSessionTokens.contains(request.getSession().getSessionToken())) {
            responseObserver.onNext(
                    DeleteItemResponse.newBuilder().setErrorDetails(INVALID_SESSION_DETAILS).build()
            );
        } else {
            if(!items.containsKey(request.getItemName())) {
                responseObserver.onNext(DeleteItemResponse.newBuilder()
                        .setErrorDetails(
                                ErrorDetails.newBuilder()
                                        .setType(ErrorType.ITEM_NOT_FOUND)
                                        .setMessage("Item does not exist")
                                        .build()
                        ).build());
            } else {
                items.remove(request.getItemName());
                responseObserver.onNext(DeleteItemResponse.newBuilder().build());
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public void hasItem(HasItemRequest request, StreamObserver<HasItemResponse> responseObserver) {
        if(!validSessionTokens.contains(request.getSession().getSessionToken())) {
            responseObserver.onNext(
                    HasItemResponse.newBuilder().setErrorDetails(INVALID_SESSION_DETAILS).build()
            );
        } else {
            responseObserver.onNext(
                    HasItemResponse.newBuilder()
                            .setHasItem(items.containsKey(request.getItemName()))
                            .build()
            );
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getAvailableItems(AvailableItemsRequest request,
                                  StreamObserver<AvailableItemsResponse> responseObserver) {
        if(!validSessionTokens.contains(request.getSession().getSessionToken())) {
            responseObserver.onNext(
                    AvailableItemsResponse.newBuilder().setErrorDetails(INVALID_SESSION_DETAILS).build()
            );
        } else {
            responseObserver.onNext(
                    AvailableItemsResponse.newBuilder()
                            .addAllItems(items.values())
                            .build()
            );
        }
        responseObserver.onCompleted();
    }

    private String generateSessionToken() {
        byte[] buff = new byte[64];
        new Random().nextBytes(buff);
        return new String(buff);
    }

}
