package bcid;

/**
 * An exception that encapsulates errors from the oauth system.
 */
public class BCIDException extends Exception {
    public BCIDException() { super(); }
    public BCIDException(String Message) { super(Message); }
    public BCIDException(String Message, Throwable cause) { super(Message, cause); }
    public BCIDException(Throwable cause) { super(cause); }
}
