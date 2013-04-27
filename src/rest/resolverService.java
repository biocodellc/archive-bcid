package rest;

import bcid.resolver;
import edu.ucsb.nceas.ezid.EZIDException;
import edu.ucsb.nceas.ezid.EZIDService;
import util.SettingsManager;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.lang.Exception;
import java.lang.String;

/**
 * The resolver service searches for identifiers in the BCID system and in EZID and returns a JSON
 * representation of results containing identifier metadata.
 * One can parse the JSON result for "Error" for non-responses or bad identifiers.
 * This service is open to ALL and does not require authentication.
 * <p/>
 * Resolution determines if this is a Data Group, a Data Element with an encoded ID, or a
 * Data Element with a suffix.
 */
@Path("resolverService")
public class resolverService {
    static SettingsManager sm;
    @Context
    static ServletContext context;

    /**
     * Load settings manager
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * User passes in an identifier of the form scheme:/naan/shoulder_identifier
     *
     * @param scheme
     * @param naan
     * @param shoulderPlusIdentifier
     * @return
     */
    @GET
    @Path("/{scheme}/{naan}/{shoulderPlusIdentifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@PathParam("scheme") String scheme,
                      @PathParam("naan") String naan,
                      @PathParam("shoulderPlusIdentifier") String shoulderPlusIdentifier) {

        // Clean up input
        scheme = scheme.trim();        
        shoulderPlusIdentifier = shoulderPlusIdentifier.trim();
        
        // Structure the identifier element from path parameters
        String element = scheme + "/" + naan + "/" + shoulderPlusIdentifier;

        // SettingsManager
        SettingsManager sm = SettingsManager.getInstance();
        try {
            sm.loadProperties();
        } catch (FileNotFoundException e) {
            return new serviceErrorReporter(e, "Unable to load properties file on server").json();
        }

        // Setup ezid account/login information
        EZIDService ezidService = new EZIDService();
        try {
            ezidService.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
        } catch (EZIDException e) {
            // For now, just print stack trace here and proceed.
            e.printStackTrace();
        }
        
        // Run Resolver
        try {
            return new resolver(element).resolveAllAsJSON(ezidService);                               
        } catch (EZIDException e) {
            return new serviceErrorReporter(e).json();
        } catch (Exception e) {
            return new serviceErrorReporter(e).json();
        }
    }    
}
