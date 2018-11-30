package kvbb.jannis.grpc.exceptions;

import kvbb.jannis.grpc.ErrorDetails;
import kvbb.jannis.grpc.ErrorType;

public class DetailedErrorException extends RPCException {
    private ErrorType type;
    private String message;

    public DetailedErrorException() {
        super();
        type = ErrorType.OTHER;
    }

    public DetailedErrorException(ErrorDetails errorDetails) {
        super();
        if(errorDetails != null) {
            type = errorDetails.getType();
            message = errorDetails.getMessage();
        }
    }

    public ErrorType getType() {
        return type;
    }

    @Override
    public String getMessage() {
        if(message == null) {
            return super.getMessage();
        }
        return message;
    }
}
