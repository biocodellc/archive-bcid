package util;

import javax.servlet.http.HttpServletRequest;

/**
 * A data class to provide information to the user about exceptions thrown in a service
 */
public class errorInfo {
    private Integer statusCode;
    private String message;
    private String requestURI;
    private String throwable;

    public errorInfo(Throwable throwable, HttpServletRequest request) {
        this.statusCode = 500;
        this.message = throwable.getMessage();
        this.requestURI = request.getRequestURI();
        this.throwable = throwable.getClass().toString();
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getThrowable() {
        return throwable;
    }

    public String toJSON() {
        return "{\"error\": {" +
                    "\"Request_URI\": \"" + requestURI + "\"," +
                    "\"Status_Code\": \"" + statusCode + "\"," +
                    "\"Exception\": \"" + throwable + "\"," +
                    "\"Message\": \"" + message + "\"" +
                "}}";
    }

    public String toHTMLTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<th colspan=2><font color=#d73027>Server Error</font></th>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Request that Failed:</td>\n");
        sb.append("\t\t<td>" + requestURI + "</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Status Code:</td>\n");
        sb.append("\t\t<td>" + statusCode + "</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Exception:</td>\n");
        sb.append("\t\t<td>" + throwable + "</td>\n");
        sb.append("\t</tr>\n");

        sb.append("\t<tr>\n");
        sb.append("\t\t<td>Message:</td>\n");
        sb.append("\t\t<td>" + message + "</td>\n");
        sb.append("\t</tr>\n");

        sb.append("</table>");

        return sb.toString();

    }

}
