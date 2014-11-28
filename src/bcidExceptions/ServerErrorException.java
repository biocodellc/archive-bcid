package bcidExceptions;

import javax.ws.rs.core.Response;

/**
 * An exception that encapsulates server errors
 */
public class ServerErrorException extends BCIDAbstractException {
    private static Integer httpStatusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

    public ServerErrorException(String usrMessage, Throwable cause) {
        super(usrMessage, httpStatusCode, cause);
    }

    public ServerErrorException(Throwable cause) {
        super("Server Error", httpStatusCode, cause);
    }

    public ServerErrorException(String usrMessage, String developerMessage, Throwable cause) {
        super(usrMessage, developerMessage, httpStatusCode, cause);
    }
}