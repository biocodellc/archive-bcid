package rest;

import bcid.database;
import bcid.projectMinter;
import bcid.resolver;
import bcid.expeditionMinter;


import util.SettingsManager;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

/**
 * REST interface calls for working with expeditions.  This includes creating, updating and deleting expeditions.
 */
@Path("expeditionService")
public class expeditionService {

    @Context
    static ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/associate")
    public Response mint(@FormParam("expedition_code") String expedition_code,
                         @FormParam("bcid") String bcid,
                         @FormParam("project_id") Integer project_id) {
        expeditionMinter expedition = null;
        try {
            expedition = new expeditionMinter();
            expedition.attachReferenceToExpedition(expedition_code, bcid, project_id);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }
        return Response.ok("Succesfully associated expedition_code = " + expedition_code + " with bcid = " + bcid).build();

    }

    /**
     * validateExpedition service checks the status of a new expedition code on the server and directing consuming
     * applications on whether this user owns the expedition and if it exists within an project or not.
     * Responses are error, update, or insert (first term followed by a colon)
     * @param expedition_code
     * @param project_id
     * @param request
     * @return
     * @throws Exception
     */
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Path("/validateExpedition/{project_id}/{expedition_code}")
    public Response mint(@PathParam("expedition_code") String expedition_code,
                         @PathParam("project_id") Integer project_id,
                         @Context HttpServletRequest request) throws Exception {

        // Get the user_id
        database db = new database();
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return Response.ok("error: user not validated").build();
        }

        Integer user_id = db.getUserId(username.toString());


         expeditionMinter expedition = null;

        try {
            // Mint a expedition
            expedition = new expeditionMinter();
            //System.out.println("checking user_id = " + user_id + " & expedition_code = " + expedition_code);
            if (!expedition.userExistsInProject(user_id, project_id)) {
                // If the user isn't in the project, then we can't update or create a new expedition
                return Response.ok("error: user is not authorized to update/create expeditions in this project").build();
            } else if (expedition.userOwnsExpedition(user_id,expedition_code, project_id)) {
                // If the user already owns the expedition, then great--- this is an update
                return Response.ok("update: user owns this expedition").build();
                // If the expedition exists in the project but the user does not own the expedition then this means we can't
            } else if (expedition.expeditionExistsInProject(expedition_code,project_id)) {
                return Response.ok("error: expedition already exists within this project but the user does not own it").build();
            } else {
                return Response.ok("insert: the expedition does not exist with project and nobody owns it").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response mint(@FormParam("expedition_code") String expedition_code,
                         @FormParam("expedition_title") String expedition_title,
                         @FormParam("project_id") Integer project_id,
                         @FormParam("public") Boolean isPublic,
                         @Context HttpServletRequest request) throws Exception {

        // Get the user_id
        database db = new database();
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        //
        if (isPublic == null) {
            isPublic = true;
        }

        if (username == null) {
            return Response.status(401).build();
        }

        Integer user_id = db.getUserId(username.toString());

        Integer expedition_id = null;
        expeditionMinter expedition = null;

        try {
            // Mint a expedition
            expedition = new expeditionMinter();
            expedition_id = expedition.mint(
                    expedition_code,
                    expedition_title,
                    user_id,
                    project_id,
                    isPublic
                    );

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }


        // Initialize settings manager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("ERROR: " + e.getMessage()).build();
        }

        // Send an Email that this completed
        sendEmail sendEmail = new sendEmail(
                sm.retrieveValue("mailUser"),
                sm.retrieveValue("mailPassword"),
                sm.retrieveValue("mailFrom"),
                sm.retrieveValue("mailTo"),
                "New Expedition",
                expedition.printMetadata(expedition_id));
        sendEmail.start();

        return Response.ok("Succesfully created expedition:<br>" + expedition.printMetadataHTML(expedition_id)).build();
    }


    /**
     * Given a expedition code and a resource alias, return a BCID
     *
     * @param expedition
     * @param resourceAlias
     * @return
     * @throws Exception
     */
    @GET
    @Path("/{project_id}/{expedition}/{resourceAlias}")
    @Produces(MediaType.TEXT_HTML)
    public Response fetchAlias(@PathParam("expedition") String expedition,
                               @PathParam("project_id") Integer project_id,
                               @PathParam("resourceAlias") String resourceAlias) throws Exception {

        resolver r = new resolver(expedition, project_id, resourceAlias);
        String response = r.getArk();
        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(r.getArk()).build();
        }
    }

       /**
     * Given an project and a expedition code return a list of resource Types associated with it
     *
     * @param expedition
     * @return
     * @throws Exception
     */
    @GET
    @Path("/deepRoots/{project_id}/{expedition}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchDeepRoots(@PathParam("expedition") String expedition,
                                   @PathParam("project_id") Integer project_id) throws Exception {
        expeditionMinter expeditionMinter = new expeditionMinter();

        String response = expeditionMinter.getDeepRoots(expedition, project_id);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).build();
        }
    }

    @GET
    @Path("/list/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String listExpeditions(@PathParam("project_id") Integer projectId,
                                  @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "[{\"error\": \"You must be logged in to view your expeditions.\"}]";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            return e.listExpeditions(projectId, username.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[{\"error\": \"server error\"}]";
    }

    @GET
    @Path("resourcesAsTable/{expedition_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listResourcesAsTable(@PathParam("expedition_id") Integer expeditionId,
                                       @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to view this expeditions resources.";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            return e.listExpeditionResourcesAsTable(expeditionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Server Error";

    }

    @GET
    @Path("datasetsAsTable/{expedition_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listDatasetsAsTable(@PathParam("expedition_id") Integer expeditionId,
                                      @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to view this expeditions datasets.";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            return e.listExpeditionDatasetsAsTable(expeditionId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Server Error";
    }

    @GET
    @Path("/admin/listExpeditionsAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listExpeditionAsTable(@PathParam("project_id") Integer projectId,
                                        @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object admin = session.getAttribute("projectAdmin");
        String username = session.getAttribute("user").toString();

        if (admin == null) {
            return "You must be this project's admin in order to view its expeditions.";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            return e.listExpeditionsAsTable(projectId, username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Server error fetching expeditions.";
    }

    @POST
    @Path("/admin/publicExpeditions")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String publicExpeditions(MultivaluedMap<String, String> data,
                                    @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Integer projectId = new Integer(data.remove("project_id").get(0));

        if (username == null) {
            return "[{\"error\": \"You must be logged in to update an expedition's public status.\"}]";
        }

        try {
            database db = new database();
            projectMinter p = new projectMinter();
            Integer userId = db.getUserId(username.toString());

            if (!p.userProjectAdmin(userId, projectId)) {
                return "[{\"error\": \"You must be this project's admin in order to update a project expedition's public status.\"}]";
            }
            expeditionMinter e = new expeditionMinter();

            if (e.updateExpeditionsPublicStatus(data, projectId)) {
                return "[{\"success\": \"successfully updated.\"}]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[{\"error\": \"server error\"}]";
    }

}

