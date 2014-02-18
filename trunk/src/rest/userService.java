package rest;

import bcid.profileRetriever;
import bcid.userMinter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created by rjewing on 2/11/14.
 */
@Path("userService")
public class userService {

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSystemUsers(@Context HttpServletRequest request)
            throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only display system users to project admins
            return "[{}]";
        }

        userMinter u = new userMinter();
        return u.listSystemUsers();
    }

    @POST
    @Path("/add")
    public void addUser(@FormParam("projectId") Integer projectId,
                        @FormParam("userId") Integer userId,
                        @Context HttpServletRequest request,
                        @Context HttpServletResponse response)
        throws IOException {

        HttpSession session = request.getSession();
        Boolean success = false;

        if (session.getAttribute("projectAdmin") == null) {
            response.sendError(403);
            return;
        }

        try {
            userMinter u = new userMinter();
            success = u.addUserToProject(userId, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            response.sendRedirect("/bcid/secure/user.jsp");
            return;
        }

        response.sendRedirect("/bcid/secure/user.jsp?addError");
    }

    @POST
    @Path("/create")
    public void createUser(@FormParam("username") String username,
                           @FormParam("password") String password,
                           @FormParam("projectId") Integer projectId,
                           @Context HttpServletRequest request,
                           @Context HttpServletResponse response)
        throws IOException {

        HttpSession session = request.getSession();
        Boolean success = false;

        if (session.getAttribute("projectAdmin") == null) {
            // only project admins are able to create users
            response.sendError(403);
            return;
        }

        try {
            userMinter u = new userMinter();
            success = u.createUser(username, password, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            response.sendRedirect("/bcid/secure/user.jsp");
            return;
        }

        response.sendRedirect("/bcid/secure/user.jsp?createError");
    }
}
