package bcidExceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * An exception that encapsulates bad requests
 */
public class BadRequestException extends WebApplicationException {
    String usrMessage;
    String developerMessage;

    String url;

    Integer httpStatusCode = Response.Status.BAD_REQUEST.getStatusCode();

    public BadRequestException(String usrMessage) {
        super();
        this.usrMessage = usrMessage;
    }

    public BadRequestException(String usrMessage, String url) {
        super();
        this.usrMessage = usrMessage;
        this.url = url;
    }

    public BadRequestException(String usrMessage, String developerMessage, String url) {
        super();
        this.usrMessage = usrMessage;
        this.developerMessage = developerMessage;
        this.url = url;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getUrl() {
        return url;
    }
}
