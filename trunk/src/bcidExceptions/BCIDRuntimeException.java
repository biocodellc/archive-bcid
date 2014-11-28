package bcidExceptions;

/**
 * An exception that encapsulates errors from the bcid system.
 */
public class BCIDRuntimeException extends RuntimeException {
    private Integer httpStatusCode;
    private String usrMessage;
    private String developerMessage;


    public BCIDRuntimeException(String usrMessage, String developerMessage, Integer httpStatusCode) {
        super(developerMessage);
        this.usrMessage = usrMessage;
        this.developerMessage = developerMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public BCIDRuntimeException(String developerMessage,  Integer httpStatusCode, Throwable cause) {
        super(developerMessage, cause);
        this.developerMessage = developerMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public BCIDRuntimeException(String usrMessage, String developerMessage, Integer httpStatusCode, Throwable cause) {
        super(developerMessage, cause);
        this.developerMessage = developerMessage;
        this.usrMessage = usrMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getUsrMessage() {
        return usrMessage;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }
}
