package rest;

import auth.oauth2.provider;
import bcid.database;
import bcid.projectMinter;
import bcid.resolver;
import bcid.expeditionMinter;


import util.SettingsManager;
import util.errorInfo;
import util.sendEmail;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * REST interface calls for working with expeditions.  This includes creating, updating and deleting expeditions.
 */
@Path("expeditionService")
public class expeditionService {

    @Context
    static ServletContext context;
    @Context HttpServletRequest request;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/associate")
    public Response mint(@FormParam("expedition_code") String expedition_code,
                         @FormParam("bcid") String bcid,
                         @FormParam("project_id") Integer project_id) {
        try {
            expeditionMinter expedition = null;
            expedition = new expeditionMinter();
            expedition.attachReferenceToExpedition(expedition_code, bcid, project_id);

            return Response.ok("{\"success\": \"Succesfully associated dataset_code = " + expedition_code +
                    " with bcid = " + bcid + "\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }

    }

    /**
     * validateExpedition service checks the status of a new expedition code on the server and directing consuming
     * applications on whether this user owns the expedition and if it exists within an project or not.
     * Responses are error, update, or insert (first term followed by a colon)
     * @param expedition_code
     * @param project_id
     * @return
     */
    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/validateExpedition/{project_id}/{expedition_code}")
    public Response mint(@PathParam("expedition_code") String expedition_code,
                         @PathParam("project_id") Integer project_id,
                         @QueryParam("access_token") String accessToken) {
        String username;
        try {
            expedition_code = URLDecoder.decode(expedition_code, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            // if accessToken != null, then OAuth client is accessing on behalf of a user
            if (accessToken != null) {
                provider p = new provider();
                username = p.validateToken(accessToken);
            } else {
                HttpSession session = request.getSession();
                username = (String) session.getAttribute("user");
            }

            if (username == null) {
                // status=401 means unauthorized user
                return Response.status(401).entity("{\"error\": \"Please login.\"}").build();
                //return Response.status(401).entity("{\"error\": \"dataset already exists within this project but the user does not own it\"}").build();
            }
            // Get the user_id
            database db = new database();
            Integer user_id = db.getUserId(username);

            expeditionMinter expedition = null;

            // Mint a expedition
            expedition = new expeditionMinter();
            //System.out.println("checking user_id = " + user_id + " & expedition_code = " + expedition_code);
            if (!expedition.userExistsInProject(user_id, project_id)) {
                // If the user isn't in the project, then we can't update or create a new expedition
                return Response.status(401).entity("{\"error\": \"user is not authorized to update/create expeditions in this project\"}").build();
            } else if (expedition.userOwnsExpedition(user_id,expedition_code, project_id)) {
                // If the user already owns the expedition, then great--- this is an update
                return Response.ok("{\"update\": \"user owns this expedition\"}").build();
                // If the expedition exists in the project but the user does not own the expedition then this means we can't
            } else if (expedition.expeditionExistsInProject(expedition_code,project_id)) {
                return Response.status(401).entity("{\"error\": \"dataset already exists within this project but the user does not own it\"}").build();
            } else {
                System.out.println("user_id = " + user_id);
                System.out.println("expedition_code = " + expedition_code);
                System.out.println("project_id = " + project_id);
                return Response.ok("{\"insert\": \"the dataset does not exist with project and nobody owns it\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

    /**
     * Service for a user to mint a new expedition
     * @param expedition_code
     * @param expedition_title
     * @param project_id
     * @param isPublic
     * @param accessToken (optional) the access token that represents the user who you are minting an expedition
     *                    on behalf.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mint(@FormParam("expedition_code") String expedition_code,
                         @FormParam("expedition_title") String expedition_title,
                         @FormParam("project_id") Integer project_id,
                         @FormParam("public") Boolean isPublic,
                         @QueryParam("access_token") String accessToken) {
        String username;

        try {
            // if accessToken != null, then OAuth client is accessing on behalf of a user
            if (accessToken != null) {
                provider p = new provider();
                username = p.validateToken(accessToken);
            } else {
                HttpSession session = request.getSession();
                username = (String) session.getAttribute("user");
            }

            if (username == null) {
                // status=401 means unauthorized user
                return Response.status(401).entity("\"error\": \"unauthorized user\"}").build();
            }

            if (isPublic == null) {
                isPublic = true;
            }

            // Get the user_id
            database db = new database();
            Integer user_id = db.getUserId(username);

            Integer expedition_id = null;
            expeditionMinter expedition = null;

            // Mint a expedition
            expedition = new expeditionMinter();
            expedition_id = expedition.mint(
                    expedition_code,
                    expedition_title,
                    user_id,
                    project_id,
                    isPublic
                    );

            // Initialize settings manager
            SettingsManager sm = SettingsManager.getInstance();
            sm.loadProperties();

            // Send an Email that this completed
          sendEmail sendEmail = new sendEmail(
                    sm.retrieveValue("mailUser"),
                    sm.retrieveValue("mailPassword"),
                    sm.retrieveValue("mailFrom"),
                    sm.retrieveValue("mailTo"),
                    "New Expedition",
                    expedition.printMetadata(expedition_id));
            sendEmail.start();

            return Response.ok("{\"success\": \"Succesfully created dataset:<br>" +
                    expedition.printMetadataHTML(expedition_id) + "\"}").build();
        } catch(Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }


    /**
     * Given a expedition code and a resource alias, return a BCID
     *
     * @param expedition
     * @param resourceAlias
     * @return
     */
    @GET
    @Path("/{project_id}/{expedition}/{resourceAlias}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAlias(@PathParam("expedition") String expedition,
                               @PathParam("project_id") Integer project_id,
                               @PathParam("resourceAlias") String resourceAlias) {
        try {
            resolver r = new resolver(expedition, project_id, resourceAlias);
            String response = r.getArk();
            if (response == null) {
                return Response.status(204).entity("{\"ark\": \"\"}").build();
            } else {
                return Response.ok("{\"ark\": \"" + r.getArk() + "\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e,request).toJSON()).build();
        }
    }

       /**
     * Given an project and a expedition code return a list of resource Types associated with it
     *
     * @param expedition
     * @return
     */
    @GET
    @Path("/deepRoots/{project_id}/{expedition}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchDeepRoots(@PathParam("expedition") String expedition,
                                   @PathParam("project_id") Integer project_id) {
        try {
            expeditionMinter expeditionMinter = new expeditionMinter();

            String response = expeditionMinter.getDeepRoots(expedition, project_id);

            if (response == null) {
                return Response.status(204).build();
            } else {
                return Response.ok(response).build();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

    /**
     * Return a JSON representation of the expedition's that a user is a member of
     * @param projectId
     * @return
     */
    @GET
    @Path("/list/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listExpeditions(@PathParam("project_id") Integer projectId,
                                    @QueryParam("access_token") String accessToken) {
        String username;

        try {
            // if accessToken != null, then OAuth client is accessing on behalf of a user
            if (accessToken != null) {
                provider p = new provider();
                username = p.validateToken(accessToken);
            } else {
                HttpSession session = request.getSession();
                username = (String) session.getAttribute("user");
            }

            if (username == null) {
                return Response.status(401).entity("{\"error\": \"You must be logged in to view your datasets.\"}").build();
            }

            expeditionMinter e = new expeditionMinter();

            return Response.ok(e.listExpeditions(projectId, username.toString())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

    /**
     * Returns an HTML table of an expedition's resources
     * @param expeditionId
     * @return
     */
    @GET
    @Path("resourcesAsTable/{expedition_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listResourcesAsTable(@PathParam("expedition_id") Integer expeditionId) {
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
            return new errorInfo(e, request).toHTMLTable();
        }
    }

    /**
     * Service to retrieve an expedition's datasets as an HTML table.
     * @param expeditionId
     * @return
     */
    @GET
    @Path("datasetsAsTable/{expedition_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listDatasetsAsTable(@PathParam("expedition_id") Integer expeditionId) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "You must be logged in to view this expeditions datasets.";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            String results = e.listExpeditionDatasetsAsTable(expeditionId);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
    }

    /**
     * Service to retrieve all of the project's expeditions. For use by project admin only.
     * @param projectId
     * @return
     */
    @GET
    @Path("/admin/listExpeditionsAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String listExpeditionAsTable(@PathParam("project_id") Integer projectId) {
        HttpSession session = request.getSession();
        Object admin = session.getAttribute("projectAdmin");
        String username = session.getAttribute("user").toString();

        if (admin == null) {
            return "You must be this project's admin in order to view its datasets.";
        }

        try {
            expeditionMinter e = new expeditionMinter();
            return e.listExpeditionsAsTable(projectId, username);
        } catch (Exception e) {
            e.printStackTrace();
            return new errorInfo(e, request).toHTMLTable();
        }
    }

    /**
     * Service to retrieve all of the project's expeditions that are public.
     * @param data
     * @return
     */
    @POST
    @Path("/admin/publicExpeditions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response publicExpeditions(MultivaluedMap<String, String> data) {
        try {
            HttpSession session = request.getSession();
            Object username = session.getAttribute("user");
            Integer projectId = new Integer(data.remove("project_id").get(0));

            if (username == null) {
                return Response.status(401).entity("{\"error\": \"You must be logged in to update an dataset's public status.\"}").build();
            }

            database db = new database();
            projectMinter p = new projectMinter();
            Integer userId = db.getUserId(username.toString());

            if (!p.userProjectAdmin(userId, projectId)) {
                return Response.status(401).entity("{\"error\": \"You must be this project's admin in order to update a project dataset's public status.\"}").build();
            }
            expeditionMinter e = new expeditionMinter();

            if (e.updateExpeditionsPublicStatus(data, projectId)) {
                return Response.ok("{\"success\": \"successfully updated.\"}").build();
            } else {
                return Response.status(500).entity("{\"error\": \"Error updating dataset status.'\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(new errorInfo(e, request).toJSON()).build();
        }
    }

}

