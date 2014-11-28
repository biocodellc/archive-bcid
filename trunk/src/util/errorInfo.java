package util;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;

/**
 * A data class to provide information to the user about exceptions thrown in a service
 */
public class errorInfo {
    private Integer httpStatusCode;
    private String usrMessage;
    private String developerMessage;
    private Exception e;
    private Timestamp ts;

    public errorInfo(String usrMessage, String developerMessage, int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.usrMessage = usrMessage;
        this.developerMessage = developerMessage;
        this.ts = new Timestamp(new java.util.Date().getTime());
    }

    public errorInfo(String usrMessage,String developerMessage, int httpStatusCode, Exception e) {
        this.httpStatusCode = httpStatusCode;
        this.usrMessage = usrMessage;
        this.developerMessage = developerMessage;
        this.e = e;
        this.ts = new Timestamp(new java.util.Date().getTime());
    }
    public errorInfo(Throwable t, HttpServletRequest r) {
        this.usrMessage = t.getMessage();
    }

    public errorInfo(String usrMessage, int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
        this.usrMessage = usrMessage;
        this.ts = new Timestamp(new java.util.Date().getTime());
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return usrMessage;
    }

    public String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\t\"usrMessage\": \"" + usrMessage + "\",\n");
        sb.append("\t\"developerMessage\": \"" + developerMessage + "\",\n");
        sb.append("\t\"httpStatusCode\": \"" + httpStatusCode + "\",\n");
        sb.append("\t\"time\": \"" + ts + "\"");

        if (!e.equals(null)) {
            SettingsManager sm = SettingsManager.getInstance();
            sm.loadProperties();
            String debug = sm.retrieveValue("debug", "false");

            if (debug.equalsIgnoreCase("true")) {
                sb.append(",\n");
                sb.append("\t\"exceptionMessage\": \"" + e.getMessage() + "\",\n");
                sb.append("\t\"stackTrace\": \"" + getStackTraceString() + "\"");
            }
        }

        sb.append("\n}");
        return sb.toString();
    }

    public String toHTMLTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<th colspan=2><font color=#d73027>Server Error</font></th>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Message:</td>\n");
        sb.append("\t\t<td>" + usrMessage + "</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Status Code:</td>\n");
        sb.append("\t\t<td>" + httpStatusCode + "</td>\n");
        sb.append("\t</tr>\n");

        SettingsManager sm = SettingsManager.getInstance();
        sm.loadProperties();
        String debug = sm.retrieveValue("debug", "false");

        if (debug.equalsIgnoreCase("true")) {

            sb.append("\t<tr>\n");
            sb.append("\t\t<td>time:</td>\n");
            sb.append("\t\t<td>" + ts + "</td>\n");
            sb.append("\t</tr>\n");

            if (!e.equals(null)) {
                sb.append("\t<tr>\n");
                sb.append("\t\t<td>Exception Message:</td>\n");
                sb.append("\t\t<td>" + e.getMessage() + "</td>\n");
                sb.append("\t</tr>\n");

                sb.append("\t<tr>\n");
                sb.append("\t\t<td>Stacktrace:</td>\n");
                sb.append("\t\t<td>" + getStackTraceString() + "</td>\n");
                sb.append("\t</tr>\n");
            }
        }

        sb.append("</table>");

        return sb.toString();

    }

    // returns the full stackTrace as a string
    private String getStackTraceString() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

}
