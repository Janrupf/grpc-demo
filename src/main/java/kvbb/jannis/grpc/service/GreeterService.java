package kvbb.jannis.grpc.service;

import io.grpc.stub.StreamObserver;
import kvbb.jannis.grpc.GreeterGrpc;
import kvbb.jannis.grpc.HelloRequest;
import kvbb.jannis.grpc.HelloResponse;

public class GreeterService extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        HelloResponse response = HelloResponse.newBuilder()
                .setMessage("Hello " + request.getName())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
