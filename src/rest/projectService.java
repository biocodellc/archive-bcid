package rest;

import auth.oauth2.provider;
import bcid.database;
import bcid.projectMinter;
import bcidExceptions.BadRequestException;
import bcidExceptions.ForbiddenRequestException;
import bcidExceptions.UnauthorizedRequestException;
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

        return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
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

        return Response.ok(response).header("Access-Control-Allow-Origin", "*").build();
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
            throw new ForbiddenRequestException("You must be the project's admin.");
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

        if (username == null) {
            throw new UnauthorizedRequestException("You must be this project's admin in order to view its configuration");
        }
        projectMinter project = new projectMinter();
        return Response.ok(project.getProjectConfigAsTable(project_id, username.toString())).build();
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

        if (username == null) {
            throw new UnauthorizedRequestException("You must be this project's admin in order to edit its configuration");
        }

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
            throw new UnauthorizedRequestException("You must be logged in to edit a project's config.");
        }
        projectMinter p = new projectMinter();
        database db = new database();

        if (!p.userProjectAdmin(db.getUserId(username.toString()), projectID)) {
            throw new ForbiddenRequestException("You must be this project's admin in order to edit the config");
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
            } else {
                throw new BadRequestException("Project wasn't found");
            }
        } else {
            return Response.ok("{\"success\": \"nothing needed to be updated\"}").build();
        }
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

        projectMinter p = new projectMinter();
        database db = new database();

        if (username == null) {
            throw new UnauthorizedRequestException("You must login.");
        }
        if (!p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
            throw new ForbiddenRequestException("You are not this project's admin.");
        }

        p.removeUser(userId, projectId);

        return Response.ok("{\"success\": \"User has been successfully removed\"}").build();
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

        // userId of 0 means create new user, using ajax to create user, shouldn't ever receive userId of 0
        if (userId == 0) {
            throw new BadRequestException("invalid userId");
        }

        projectMinter p = new projectMinter();
        database db = new database();

        if (username == null) {
            throw new UnauthorizedRequestException("You must login to access this service.");
        }
        if (!p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
            throw new ForbiddenRequestException("You are not this project's admin");
        }
         p.addUserToProject(userId, projectId);

        return Response.ok("{\"success\": \"User has been successfully added to this project\"}").build();
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
            throw new ForbiddenRequestException("You are not an admin to this project");
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
        String username;

        // if accessToken != null, then OAuth client is accessing on behalf of a user
        if (accessToken != null) {
            provider p = new provider();
            username = p.validateToken(accessToken);
        } else {
            HttpSession session = request.getSession();
            username = (String) session.getAttribute("user");
        }

        if (username == null) {
            throw new UnauthorizedRequestException("authorization_error");
        }

        projectMinter p = new projectMinter();
        return Response.ok(p.listUsersProjects(username)).build();
    }
}
