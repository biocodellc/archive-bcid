package rest;

import auth.oauth2.provider;
import bcid.database;
import bcid.projectMinter;
import util.errorInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Hashtable;

/**
 * REST interface calls for working with projects.  This includes fetching details associated with projects.
 * Currently, there are no REST services for creating projects, which instead must be added to the database
 * manually by an administrator
 */
@Path("projectService")
public class projectService {

    @Context
    static HttpServletRequest request;

     /**
     * Given a project_id, return the validationXML file
     *
     * @param project_id
     * @return
     * @throws Exception
     */
    @GET
    @Path("/validation/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public Response fetchAlias(@PathParam("project_id") Integer project_id) {

        projectMinter project = new projectMinter();
        String response = project.getValidationXML(project_id);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Produce a list of all publically available projects
     *
     * @return  Generates a JSON listing containing project metadata as an array
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchList() {
        projectMinter project = new projectMinter();
        String response = project.listProjects();

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Given an project identifier, get the latest graphs by expedition
     *
     * @param project_id
     * @return
     */
    @GET
    @Path("/graphs/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestGraphsByExpedition(@PathParam("project_id") Integer project_id) {
        projectMinter project= new projectMinter();

        String response = project.getLatestGraphs(project_id);

        if (response == null) {
            return Response.status(204).build();
        } else {
            return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
        }
    }

    /**
     * Return a json representation to be used for select options of the projects that a user is an admin to
     * @return
     */
    @GET
    @Path("/admin/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserAdminProjects() {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // if not an project admin, then return nothing
            return Response.status(403).entity(new errorInfo("You must be the project's admin.", 403).toJSON()).build();
        }
        String username = session.getAttribute("user").toString();

        projectMinter project= new projectMinter();
        return Response.ok(project.listUserAdminProjects(username)).build();
    }

    /**
     * return an HTML table of a project's configuration.
     * @param project_id
     * @return
     */
    @GET
    @Path("/configAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public Response getProjectConfig(@PathParam("project_id") Integer project_id) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username != null) {
            projectMinter project = new projectMinter();
            return Response.ok(project.getProjectConfigAsTable(project_id, username.toString())).build();
        }
        return Response.status(401).entity(new errorInfo(
                "You must be this project's admin in order to view its configuration",
                401).toJSON())
                .build();
    }

    /**
     * return an HTML table used for editing a project's configuration.
     * @param projectId
     * @return
     */
    @GET
    @Path("/configEditorAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String getConfigEditorAsTable(@PathParam("project_id") Integer projectId) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        projectMinter project = new projectMinter();
        return project.getProjectConfigEditorAsTable(projectId, username.toString());
    }

    /**
     * Service used for updating a project's configuration.
     * @param projectID
     * @param title
     * @param validationXML
     * @param publicProject
     * @return
     */
    @POST
    @Path("/updateConfig/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateConfig(@PathParam("project_id") Integer projectID,
                                 @FormParam("title") String title,
                                 @FormParam("validation_xml") String validationXML,
                                 @FormParam("public") String publicProject) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null){
            return Response.status(401).entity(new errorInfo("You must be logged in to edit a project's config.", 401)
                .toJSON()).build();
        }
        projectMinter p = new projectMinter();
        database db = new database();

        if (!p.userProjectAdmin(db.getUserId(username.toString()), projectID)) {
            return Response.status(401).entity(new errorInfo("You must be this project's admin in order to edit the config",
                    401).toJSON()).build();
        }

        Hashtable config = p.getProjectConfig(projectID, username.toString());
        Hashtable<String, String> update = new Hashtable<String, String>();

        if (title != null &&
                !config.get("title").equals(title)) {
            update.put("title", title);
        }
        if (!config.containsKey("validation_xml") || !config.get("validation_xml").equals(validationXML)) {
            update.put("bioValidator_validation_xml", validationXML);
        }
        if ((publicProject != null && (publicProject.equals("on") || publicProject.equals("true")) && config.get("public").equals("false")) ||
            (publicProject == null && config.get("public").equals("true"))) {
            if (publicProject != null && (publicProject.equals("on") || publicProject.equals("true"))) {
                update.put("public", "true");
            } else {
                update.put("public", "false");
            }
        }

        if (!update.isEmpty()) {
            if (p.updateConfig(update, projectID)) {
                return Response.ok("{\"success\": \"Successfully update project config.\"}").build();
            }
        } else {
            return Response.ok("{\"success\": \"nothing needed to be updated\"}").build();
        }
        return Response.status(500).entity(new errorInfo("error updating config", 500).toJSON()).build();

    }

    /**
     * Service used to remove a user as a member of a project.
     * @param projectId
     * @param userId
     * @return
     */
    @GET
    @Path("/removeUser/{project_id}/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("project_id") Integer projectId,
                               @PathParam("user_id") Integer userId) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Boolean success = false;

        projectMinter p = new projectMinter();
        database db = new database();

        if (username == null || !p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
            return Response.status(403).entity(new errorInfo("You are not this project's admin.", 403).toJSON()).build();
        }

        success = p.removeUser(userId, projectId);

        if (success) {
            return Response.ok("{\"success\": \"User has been successfully removed\"}").build();
        }
        return Response.status(500).entity(new errorInfo("error removing user", 500).toJSON()).build();
    }

    /**
     * Service used to add a user as a member of a project.
     * @param projectId
     * @param userId
     * @return
     */
    @POST
    @Path("/addUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@FormParam("project_id") Integer projectId,
                            @FormParam("user_id") Integer userId) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Boolean success = false;

        // userId of 0 means create new user, using ajax to create user, shouldn't ever receive userId of 0
        if (userId == 0) {
            return Response.status(400).entity(new errorInfo("error creating user", 400).toJSON()).build();
        }

        projectMinter p = new projectMinter();
        database db = new database();

        if (username == null || !p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
            return Response.status(403).entity(new errorInfo("You are not this project's admin", 403).toJSON()).build();
        }
        success = p.addUserToProject(userId, projectId);

        if (success) {
            return Response.ok("{\"success\": \"User has been successfully added to this project\"}").build();
        }

        return Response.status(500).entity(new errorInfo("error adding user to project", 500).toJSON()).build();
    }

    /**
     * return an HTML table listing all members of a project
     * @param projectId
     * @return
     */
    @GET
    @Path("/listProjectUsersAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String getSystemUsers(@PathParam("project_id") Integer projectId) {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only display system users to project admins
            return "You are not an admin to this project";
        }

        projectMinter p = new projectMinter();
        return p.listProjectUsersAsTable(projectId);
    }

    /**
     * Service used to retrieve a JSON representation of the project's a user is a member of.
     * @param accessToken
     * @return
     */
    @GET
    @Path("/listUserProjects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProjects(@QueryParam("access_token") String accessToken) {
        String username = null;

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
            return Response.status(401).entity(new errorInfo("authorization_error", 401).toJSON()).build();
        }

        projectMinter p = new projectMinter();
        return Response.ok(p.listUsersProjects(username)).build();
    }
}
