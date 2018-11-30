package kvbb.jannis.grpc.exceptions;

public class RPCException extends Exception {
    public RPCException() {
        super();
    }

    public RPCException(String s) {
        super(s);
    }

    public RPCException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RPCException(Throwable throwable) {
        super(throwable);
    }

    protected RPCException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
