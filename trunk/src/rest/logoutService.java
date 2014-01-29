package rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * REST interface to logout a user
 * Created by rjewing on 1/14/14.
 */
@Path("logoutService")
public class logoutService {

    @GET
    public void logout(@Context HttpServletRequest req,
                       @Context HttpServletResponse res)
            throws IOException{

        HttpSession session = req.getSession();

        session.invalidate();
        res.sendRedirect("/bcid/index.jsp");
    }
}
