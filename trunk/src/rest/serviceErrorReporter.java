package rest;

/**
 * serviceReporter provides reporting functions for the various BCID services.  Enabling consistent
 * messaging across various services
 */
public class serviceErrorReporter {
    private Exception e;
    private String context;

    /**
     * serviceReporter instantiates this class
     * @param e
     * @param context
     */
    public serviceErrorReporter(Exception e, String context) {
        this.e = e;
        this.context = context;
        e.printStackTrace();
    }

    public serviceErrorReporter(Exception e) {
        this(e,null);
    }

    /**
     * Returns a JSON error message handler
     * @return
     */
    protected String json() {
        if (context == null) {
            return "[{\"Error\":{\"Message\":\"" + e.getMessage() + "\"}}]";
        } else {
            return "[{\"Error\":{\"Message\":\"" + context + ": " + e.getMessage() + "\"}}]";
        }
    }
}
