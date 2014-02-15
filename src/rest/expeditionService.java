package rest;

import bcid.expeditionMinter;
import bcid.projectMinter;
import bcid.userMinter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST interface calls for working with expeditions.  This includes fetching details associated with expeditions.
 * Currently, there are no REST services for creating expeditions, which instead must be added to the database
 * manually by an administrator
 */
@Path("expeditionService")
public class expeditionService {

    /**
     * Given an expedition_id
     *
     * @param expedition_id
     * @return
     * @throws Exception
     */
    @GET
    @Path("/validation/{expedition_id}")
    @Produces(MediaType.TEXT_HTML)
    public Response fetchAlias(@PathParam("expedition_id") Integer expedition_id) throws Exception {

        expeditionMinter expedition = new expeditionMinter();
        String response = expedition.getValidationXML(expedition_id);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Produce a list of all available expeditions
     *
     * @return  Generates a JSON listing containing expedition metadata as an array
     * @throws Exception
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchList() throws Exception {
        expeditionMinter expedition = new expeditionMinter();
        String response = expedition.listExpeditions();

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Given an expedition identifier, get the latest graphs by project
     *
     * @param expedition_id
     * @return
     * @throws Exception
     */
    @GET
    @Path("/graphs/{expedition_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestGraphsByProject(@PathParam("expedition_id") Integer expedition_id) throws Exception {
        expeditionMinter expedition= new expeditionMinter();

        String response = expedition.getLatestGraphs(expedition_id);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Return a json representation to be used for select options of the expeditions that a user is an admin to
     * @param request
     * @return
     * @throws Exception
     */
    @GET
    @Path("/admin/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserAdminExpeditions(@Context HttpServletRequest request)
            throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("expeditionAdmin") == null) {
            // if not an expedition admin, then return nothing
            return "[{}]";
        }
        String username = session.getAttribute("user").toString();

        expeditionMinter expedition= new expeditionMinter();
        return expedition.listUserAdminExpeditions(username);
    }
}
