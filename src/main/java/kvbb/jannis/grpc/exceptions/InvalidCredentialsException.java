package kvbb.jannis.grpc.exceptions;

public class InvalidCredentialsException extends RPCException {
    public InvalidCredentialsException() {
        super("Invalid name and password combination");
    }
    public InvalidCredentialsException(String msg) {
        super(msg);
    }
}
