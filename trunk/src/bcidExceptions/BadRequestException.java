package bcidExceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * An exception that encapsulates bad requests
 */
public class BadRequestException extends WebApplicationException {
    String usrMessage;

    String developerMessage;

    Integer httpStatusCode = Response.Status.BAD_REQUEST.getStatusCode();
    public BadRequestException(String usrMessage) {
        super();
        this.usrMessage = usrMessage;
    }

    public BadRequestException(String usrMessage, String developerMessage) {
        super();
        this.usrMessage = usrMessage;
        this.developerMessage = developerMessage;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getUsrMessage() {
        return usrMessage;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }
}
