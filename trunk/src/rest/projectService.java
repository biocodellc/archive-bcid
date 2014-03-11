package rest;

import bcid.projectMinter;
import bcid.expeditionMinter;
import bcid.userMinter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Path("/config/{project_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProjectConfig(@PathParam("project_id") Integer project_id,
                                   @Context HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object username = session.getAttribute("user");

        if (username != null) {
            try {
                projectMinter project = new projectMinter();
                return project.listProjectConfig(project_id, username.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "[{\"error\": \"You must be this project's admin in order to view its configuration\"}]";
    }
}
