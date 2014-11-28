package bcid;

/**
 * An exception that encapsulates errors from the oauth system.
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
