package auth.oauth2;

import bcid.database;
import org.apache.commons.cli.*;
import util.stringGenerator;

import java.sql.*;
import java.util.Calendar;

/**
 * This class handles all aspects of Oauth2 support.
 */
public class provider {
    protected Connection conn;

    public provider() throws Exception {
        database db = new database();
        conn = db.getConn();
    }

    /**
     * check that the given clientId is valid
     * @param clientId
     * @return
     */
    public Boolean validClientId(String clientId) {
        try {
            String selectString = "SELECT count(*) as count FROM oauthClients WHERE client_id = ?";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, clientId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") >= 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the callback url stored for the given clientID
     * @param clientID
     * @return
     * @throws SQLException
     */
    public String getCallback(String clientID) throws SQLException {
        String selectString = "SELECT callback FROM oauthClients WHERE client_id = ?";
        PreparedStatement stmt = conn.prepareStatement(selectString);

        stmt.setString(1, clientID);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("callback");
        }
        return null;
    }

    /**
     * generate a random 20 character code that can be exchanged for an access token by the client app
     * @param clientID
     * @param redirectURL
     * @param username
     * @return
     * @throws Exception
     */
    public String generateCode(String clientID, String redirectURL, String username) throws Exception {
        stringGenerator sg = new stringGenerator();
        String code = sg.generateString(20);

        database db = new database();
        Integer user_id = db.getUserId(username);

        String insertString = "INSERT INTO oauthNonces (client_id, code, user_id, redirect_uri) VALUES(?, \"" + code + "\",?,?)";
        PreparedStatement stmt = conn.prepareStatement(insertString);

        stmt.setString(1, clientID);
        stmt.setInt(2, user_id);
        stmt.setString(3, redirectURL);

        stmt.execute();
        return code;
    }

    /**
     * generate a new clientId for a oauth client app
     * @return
     */
    public String generateClientId() {
        stringGenerator sg = new stringGenerator();
        return sg.generateString(20);
    }

    /**
     * generate a client secret for a oauth client app
     * @return
     */
    public String generateClientSecret() {
        stringGenerator sg = new stringGenerator();
        return sg.generateString(75);
    }

    /**
     * verify that the given clientId and client secret match what is stored in the db
     * @param clientId
     * @param clientSecret
     * @return
     */
    public Boolean validateClient(String clientId, String clientSecret) {
        try {
            String selectString = "SELECT count(*) as count FROM oauthClients WHERE client_id = ? AND client_secret = ?";

            System.out.println("clientId = \'" + clientId + "\' clientSecret=\'" + clientSecret + "\'");


            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, clientId);
            stmt.setString(2, clientSecret);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") >= 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * verify that the given code was issued for the same client id that is trying to exchange the code for an access token
     * @param clientID
     * @param code
     * @param redirectURL
     * @return
     */
    public Boolean validateCode(String clientID, String code, String redirectURL) {
        try {
            String selectString = "SELECT current_timestamp() as current,ts FROM oauthNonces WHERE client_id = ? AND code = ? AND redirect_uri = ?";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, clientID);
            stmt.setString(2, code);
            stmt.setString(3, redirectURL);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("ts");
                // Get the current time from the database (in case the application server is in a different timezone)
                Timestamp currentTs = rs.getTimestamp("current");
                // 10 minutes previous
                Timestamp expiredTs = new Timestamp(currentTs.getTime() - 600000);

                // if ts is older then 10 mins, we can't proceed
                if (ts != null && ts.after(expiredTs)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the id of the user that the given oauth code represents
     * @param clientId
     * @param code
     * @return
     */
    private Integer getUserId(String clientId, String code) {
        Integer user_id = null;
        try {
            String selectString = "SELECT user_id FROM oauthNonces WHERE client_id=? AND code=?";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, clientId);
            stmt.setString(2, code);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user_id = rs.getInt("user_id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user_id;
    }

    /**
     * Remove the given oauth code from the db. This is called when the code is exchanged for an access token,
     * as oauth codes are only usable once.
     * @param clientId
     * @param code
     */
    private void deleteNonce(String clientId, String code) {
        try {
            String deleteString = "DELETE FROM oauthNonces WHERE client_id = ? AND code = ?";
            PreparedStatement stmt2 = conn.prepareStatement(deleteString);

            stmt2.setString(1, clientId);
            stmt2.setString(2, code);

            stmt2.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * generate a new access token given a refresh token
     * @param refreshToken
     * @return
     * @throws SQLException
     */
    public String generateToken(String refreshToken)
        throws SQLException {
        Integer userId = null;
        String clientId = null;

        String sql = "SELECT client_id, user_id FROM oauthTokens WHERE refresh_token = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, refreshToken);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            userId = rs.getInt("user_id");
            clientId = rs.getString("client_id");
        }

        if (userId == null) {
            return "{\"error\": \"server_error\"}";
        }

        return generateToken(clientId, userId, null);

    }

    /**
     * generate an access token given a code, clientID, and state (optional)
     * @param clientID
     * @param state
     * @param code
     * @return
     * @throws SQLException
     */
    public String generateToken(String clientID, String state, String code) throws SQLException{
        Integer user_id = getUserId(clientID, code);
        deleteNonce(clientID, code);
        if (user_id == null) {
            return "{\"error\": \"server_error\"}";
        }

        return generateToken(clientID, user_id, state);
    }

    /**
     * generate an oauth compliant JSON response with an access_token and refresh_token
     * @param clientID
     * @param userId
     * @param state
     * @return
     * @throws SQLException
     */
    private String generateToken(String clientID, Integer userId, String state)
        throws SQLException {
        stringGenerator sg = new stringGenerator();
        String token = sg.generateString(20);
        String refreshToken = sg.generateString(20);

        String insertString = "INSERT INTO oauthTokens (client_id, token, refresh_token, user_id) VALUE " +
                "(?, \"" + token +"\",\"" + refreshToken + "\", ?)";
        PreparedStatement stmt = conn.prepareStatement(insertString);

        stmt.setString(1, clientID);
        stmt.setInt(2, userId);
        stmt.execute();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"access_token\":\"" + token + "\",\n");
        sb.append("\"refresh_token\":\"" + refreshToken + "\",\n");
        sb.append("\"token_type\":\"bearer\",\n");
        sb.append("\"expires_in\":3600\n");
        if (state != null) {
            sb.append(("\"state\":\"" + state + "\""));
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * delete an access_token. This is called when a refresh_token has been exchanged for a new access_token.
     * @param refreshToken
     */
    public void deleteAccessToken(String refreshToken) {
        try {
            String deleteString = "DELETE FROM oauthTokens WHERE refresh_token = ?";
            PreparedStatement stmt2 = conn.prepareStatement(deleteString);

            stmt2.setString(1, refreshToken);

            stmt2.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * verify that a refresh token is still valid
     * @param refreshToken
     * @return
     */
    public Boolean validateRefreshToken(String refreshToken) {
        try {
            String sql = "SELECT current_timestamp() as current,ts FROM oauthTokens WHERE refresh_token = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, refreshToken);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("ts");

                  // Get the current time from the database (in case the application server is in a different timezone)
                Timestamp currentTs = rs.getTimestamp("current");
                // get a Timestamp instance for  for 24 hrs ago
                Timestamp expiredTs = new Timestamp(currentTs.getTime() - 86400000);

                // if ts is older 24 hrs, we can't proceed
                if (ts != null && ts.after(expiredTs)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verify that an access token is still valid. Access tokens are only good for 1 hour.
     * @param token the access_token issued to the client
     * @return username the token represents, null if invalid token
     */
    public String validateToken(String token) {
        try {
            String selectString = "SELECT current_timestamp() as current,t.ts as ts, u.username as username "+
                    "FROM oauthTokens t, users u WHERE t.token=? && u.user_id = t.user_id";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                Timestamp ts = rs.getTimestamp("ts");

                 // Get the current time from the database (in case the application server is in a different timezone)
                Timestamp currentTs = rs.getTimestamp("current");
                // get a Timestamp instance for 1 hr ago
                Timestamp expiredTs = new Timestamp(currentTs.getTime() - 3600000);
                // if ts is older then 1 hr, we can't proceed
                if (ts != null && ts.after(expiredTs)) {
                    return rs.getString("username");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Given a hostname, register a client app for oauth use. Will generate a new client id and client secret
     * for the client app
     * @param args
     */
    public static void main(String args[]) {
        // Some classes to help us
        CommandLineParser clp = new GnuParser();
        CommandLine cl;

        Options options = new Options();
        options.addOption("c", "callback url", true, "The callback url of the client app");

        try {
            cl = clp.parse(options, args);
        } catch (UnrecognizedOptionException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        if (!cl.hasOption("c")) {
            System.out.println("You must enter a callback url");
            return;
        }

        String host = cl.getOptionValue("c");
        try {
            provider p = new provider();

            String clientId = p.generateClientId();
            String clientSecret = p.generateClientSecret();

            //String insertString = "INSERT INTO oauthClients (client_id, client_secret, callback) VALUES (\""
            //                      + clientId + "\",\"" + clientSecret + "\",?)";

            System.out.println("USE THE FOLLOWING INSERT STATEMENT IN YOUR DATABASE:\n\n");
            System.out.println("INSERT INTO oauthClients (client_id, client_secret, callback) VALUES (\""
                                  + clientId + "\",\"" + clientSecret + "\",\"" + host +"\")");
            System.out.println(".\nYou will need the following information:\n\nclient_id: "
                    + clientId + "\nclient_secret: " + clientSecret);
            /*PreparedStatement stmt = p.conn.prepareStatement(insertString);

            stmt.setString(1, host);
            stmt.execute();

            System.out.println("Successfully registered oauth2 client app at host: " + host
                    + ".\nYou will need the following information:\n\nclient_id: "
                    + clientId + "\nclient_secret: " + clientSecret);
                    */
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
