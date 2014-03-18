package rest;

import bcid.database;
import bcid.projectMinter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public Response fetchAlias(@PathParam("project_id") Integer project_id) throws Exception {

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
     * @throws Exception
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchList() throws Exception {
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
     * @throws Exception
     */
    @GET
    @Path("/graphs/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestGraphsByExpedition(@PathParam("project_id") Integer project_id) throws Exception {
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
     * @param request
     * @return
     * @throws Exception
     */
    @GET
    @Path("/admin/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserAdminProjects(@Context HttpServletRequest request)
            throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // if not an project admin, then return nothing
            return "[{}]";
        }
        String username = session.getAttribute("user").toString();

        projectMinter project= new projectMinter();
        return project.listUserAdminProjects(username);
    }

    @GET
    @Path("/configAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String getProjectConfig(@PathParam("project_id") Integer project_id,
                                   @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username != null) {
            try {
                projectMinter project = new projectMinter();
                return project.getProjectConfigAsTable(project_id, username.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "[{\"error\": \"You must be this project's admin in order to view its configuration\"}]";
    }

    @GET
    @Path("/configEditorAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String getConfigEditorAsTable(@PathParam("project_id") Integer projectId,
                                         @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        try {
            projectMinter project = new projectMinter();
            return project.getProjectConfigEditorAsTable(projectId, username.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "server error loading project config editor";
    }

    @POST
    @Path("/updateConfig/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateConfig(@PathParam("project_id") Integer projectID,
                               @FormParam("title") String title,
                               @FormParam("validation_xml") String validationXML,
                               @FormParam("public") String publicProject,
                               @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null){
            return "[{\"error\": \"You must be logged in to edit a project's config.\"}]";
        }
        try {
            projectMinter p = new projectMinter();
            database db = new database();

            if (!p.userProjectAdmin(db.getUserId(username.toString()), projectID)) {
                return "[{\"error\": \"You must be this project's admin in order to edit the config\"}]";
            }

            Hashtable config = p.getProjectConfig(projectID, username.toString());
            Hashtable<String, String> update = new Hashtable<String, String>();

            if (!config.get("title").equals(title)) {
                update.put("title", title);
            }
            if (!config.containsKey("validation_xml") || !config.get("validation_xml").equals(validationXML)) {
                update.put("validation_xml", validationXML);
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
                    return "[{\"success\": \"Successfully update project config.\"}]";
                }
            } else {
                return "[{\"success\": \"nothing needed to be updated\"}]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[{\"error\": \"server error\"}]";

    }

    @GET
    @Path("/removeUser/{project_id}/{user_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeUser(@PathParam("project_id") Integer projectId,
                             @PathParam("user_id") Integer userId,
                             @Context HttpServletRequest request)
        throws IOException {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Boolean success = false;

        try {
            projectMinter p = new projectMinter();
            database db = new database();

            if (username == null || !p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
                return "[{\"error\": \"You are not this project's admin\"}]";
            }

            success = p.removeUser(userId, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            return "[{\"success\": \"User has been successfully removed\"}]";
        }

        return "[{\"error\": \"server error\"}]";

    }
    @POST
    @Path("/addUser")
    @Produces(MediaType.APPLICATION_JSON)
    public String addUser(@FormParam("project_id") Integer projectId,
                          @FormParam("user_id") Integer userId,
                          @Context HttpServletRequest request)
            throws IOException {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");
        Boolean success = false;

        // userId of 0 means create new user, using ajax to create user, shouldn't ever receive userId of 0
        if (userId == 0) {
            return "[{\"error\": \"error creating user\"}]";
        }

        try {
            projectMinter p = new projectMinter();
            database db = new database();

            if (username == null || !p.userProjectAdmin(db.getUserId(username.toString()), projectId)) {
                return "[{\"error\": \"You are not this project's admin\"}]";
            }
            success = p.addUserToProject(userId, projectId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success) {
            return "[{\"success\": \"User has been successfully added to this project\"}]";
        }

        return "[{\"error\": \"server error\"}]";
    }

    @GET
    @Path("/listProjectUsersAsTable/{project_id}")
    @Produces(MediaType.TEXT_HTML)
    public String getSystemUsers(@PathParam("project_id") Integer projectId,
                                 @Context HttpServletRequest request)
            throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute("projectAdmin") == null) {
            // only display system users to project admins
            return "[{\"error\": \"You are not an admin to this project\"}]";
        }

        projectMinter p = new projectMinter();
        return p.listProjectUsersAsTable(projectId);
    }

    @GET
    @Path("/listUserProjects")
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserProjects(@Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username == null) {
            return "[{\"error\": \"You must be logged in to view your projects\"}]";
        }

        try {
            projectMinter p = new projectMinter();
            return p.listUsersProjects(username.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[{\"error\": \"server_error\"}]";
    }
}
