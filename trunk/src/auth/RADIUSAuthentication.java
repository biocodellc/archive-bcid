package auth;

import bcidExceptions.ServerErrorException;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.util.RadiusException;
import util.SettingsManager;

import java.io.IOException;

/**
 * Authenticate using a RADIUS server. This class uses the tinyradius library found at http://tinyradius.sourceforge.net/
 */
public class RADIUSAuthentication {
    static SettingsManager sm;

    private String username;
    private String password;
    static String radiusServerIp;
    static String radiusSecret;
    
    /**
     * Load settings manager
     */
    static {
        // Initialize settings manager
        sm = SettingsManager.getInstance();
        sm.loadProperties();
        // Get the RADIUS server & secret from property file
        radiusServerIp = sm.retrieveValue("radiusServerIp");
        radiusSecret = sm.retrieveValue("radiusSecret");
    }

    public RADIUSAuthentication(String usr, String pass) {
        username = usr;
        password = pass;
    }
    public Boolean authenticate() {
        RadiusClient rc = new RadiusClient(radiusServerIp, radiusSecret);
        AccessRequest ar = new AccessRequest(username, password);

        // I believe AUTH_PAP is correct. When using AUTH_CHAP, the response atrribute had Reply-Message:
        // Radius password protection using CHAP does not support External authentication.
        ar.setAuthProtocol(AccessRequest.AUTH_PAP);

        // Are there any specific attributes that I need to set for the Access-Request?

        try {
            // send the Access-Request
            RadiusPacket response = rc.authenticate(ar);

            // The response is of packet type Access-Reject with the following Reply-Message:
            // Failed authentication for user XXX. Invalid response to a challenge. X EXTERNAL authentication attempt remaining.
            // I think that we should be receiving an Access-Challenge response, but we aren't.

            if (response.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
                // the authentication was successful
            } else if (response.getPacketType() == RadiusPacket.ACCESS_CHALLENGE) {
                // need to get more information from the user

                // get user input for security questions will be in the attributes

                // send another Access-Challenge to the radius server with the question answers

                // receive either an Access-Accept or Access-Reject packet from the radius server
            }

            // If we are here, the server rejected the Access-Request

        } catch (RadiusException e) {
            throw new ServerErrorException(e);
        } catch (IOException e) {
            throw new ServerErrorException(e);
        }
    }
}
