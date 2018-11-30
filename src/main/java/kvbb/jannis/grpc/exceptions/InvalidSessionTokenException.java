package kvbb.jannis.grpc.exceptions;

public class InvalidSessionTokenException extends RPCException {
    public InvalidSessionTokenException() {
        super("Invalid session token");
    }
}
