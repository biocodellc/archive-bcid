package bcidExceptions;

/**
 * An exception that encapsulates errors from the bcid system.
 */
public class BCIDRuntimeException extends RuntimeException {
    private Integer httpStatusCode;
    private String usrMessage;
    private String developerMessage;

    public BCIDRuntimeException() {
        super();
    }

    public BCIDRuntimeException(String developerMessage) {
        super(developerMessage);
        this.developerMessage = developerMessage;
    }

    public BCIDRuntimeException(String usrMessage,  Integer httpStatusCode, Throwable cause) {
        super(usrMessage, cause);
        this.usrMessage = usrMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public BCIDRuntimeException(String usrMessage, String developerMessage, Integer httpStatusCode, Throwable cause) {
        super(developerMessage, cause);
        this.developerMessage = developerMessage;
        this.usrMessage = usrMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public BCIDRuntimeException(String developerMessage, Throwable cause) {
        super(developerMessage, cause);
        this.developerMessage = developerMessage;
    }

    public BCIDRuntimeException(Throwable cause) {
        super(cause);
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
