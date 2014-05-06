package rest;

import bcid.Renderer.RDFRenderer;
import bcid.resolver;
import util.SettingsManager;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.lang.Exception;
import java.lang.String;
import java.net.URI;

/**
 * This is the core resolver Service for BCIDs.  It returns URIs
 */
@Path("ark:")
public class resolverService {

    String scheme = "ark:";
    @Context
    static ServletContext context;

    /**
     * User passes in an identifier of the form scheme:/naan/shoulder_identifier
     *
     * @param naan
     * @param shoulderPlusIdentifier
     * @return
     */
    @GET
    @Path("/{naan}/{shoulderPlusIdentifier}")
    @Produces({MediaType.TEXT_HTML, "application/rdf+xml"})
    public Response run(
            @PathParam("naan") String naan,
            @PathParam("shoulderPlusIdentifier") String shoulderPlusIdentifier,
            @HeaderParam("accept") String accept) {

        shoulderPlusIdentifier = shoulderPlusIdentifier.trim();

        // Structure the identifier element from path parameters
        String element = scheme + "/" + naan + "/" + shoulderPlusIdentifier;

        // When the Accept Header = "application/rdf+xml" return Metadata as RDF
        if (accept.equalsIgnoreCase("application/rdf+xml")) {
            try {
                return Response.ok(new resolver(element).printMetadata(new RDFRenderer())).build();
            } catch (Exception e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
        // All other Accept Headers, or none specified, then attempt a redirect
        } else {
            URI seeOtherUri = null;
            try {
                seeOtherUri = new resolver(element).resolveARK();
                System.out.println(seeOtherUri);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return Response.status(303).location(seeOtherUri).build();
        }
    }
}
