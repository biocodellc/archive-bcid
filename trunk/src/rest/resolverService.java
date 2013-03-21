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
 * Resolver Service Call, returning JSON representation of results containing identifier metadata.
 * One can parse the JSON result for "Error" for non-responses, bad identifiers.
 * This service should be open to ALL and not require user authentication.
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String run(@FormParam("identifier") String identifier) {
        // Initialize variables
        SettingsManager sm = SettingsManager.getInstance();
        EZIDService ezidAccount = new EZIDService();
        // Setup ezid account/login information
        try {
            sm.loadProperties();
            ezidAccount.login(sm.retrieveValue("eziduser"), sm.retrieveValue("ezidpass"));
            return resolverResults(ezidAccount, identifier);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "{\"Error\":\"" + e.getMessage() + "\"}";
        } catch (EZIDException e) {
            e.printStackTrace();
            return "{\"Error\":\"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"Error\":\"" + e.getMessage() + "\"}";
        }
    }

    private String resolverResults(EZIDService ezidService, String identifier) throws Exception {
        return new resolver(identifier).resolveAll(ezidService);
    }
}
