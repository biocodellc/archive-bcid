package rest;

import util.errorInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.URI;

/**
 * class to catch an exception thrown from a rest service and map the necessary information to a request
 */
@Provider
public class exceptionMapper implements ExceptionMapper<Throwable> {
    @Context
    static HttpServletRequest request;

    @Override
    public Response toResponse(Throwable throwable) {
        errorInfo errorInfo = new errorInfo(throwable, request);
        throwable.printStackTrace();

        URI uri = null;
        try {
            // send the user to error.jsp to display info about the exception/error
            uri = new URI("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uri == null) {
            // shouldn't get here, but just in case, return the errorInfo object
            return Response.status(500).entity(errorInfo.toJSON()).build();
        }

        HttpSession session = request.getSession();
        session.setAttribute("errorInfo", errorInfo);

        return Response.status(500).location(uri).build();
    }
}
