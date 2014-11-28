package auth.oauth2;

/**
 * An exception that encapsulates errors from the oauth system.
 */
public class OAUTHException extends Exception {
    public OAUTHException() { super(); }
    public OAUTHException(String message) { super(message); }
    public OAUTHException(String message, Throwable cause) { super(message, cause); }
    public OAUTHException(Throwable cause) { super(cause); }
}
