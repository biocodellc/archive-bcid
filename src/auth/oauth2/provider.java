package auth.oauth2;

import bcid.database;
import org.apache.commons.cli.*;
import util.stringGenerator;

import java.sql.*;
import java.util.Calendar;

/**
 * Created by rjewing on 2/15/14.
 */
public class provider {
    protected Connection conn;

    public provider() throws Exception {
        database db = new database();
        conn = db.getConn();
    }

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

    public String generateCode(String clientID) throws SQLException {
        // TODO bound code to redirect_uri?
        stringGenerator sg = new stringGenerator();
        String code = sg.generateString(20);

        String insertString = "INSERT INTO oauthNonces (client_id, code) VALUES(?, \"" + code + "\")";
        PreparedStatement stmt = conn.prepareStatement(insertString);

        stmt.setString(1, clientID);

        stmt.execute();
        return code;
    }

    public String generateClientId() {
        stringGenerator sg = new stringGenerator();
        return sg.generateString(20);
    }

    public String generateClientSecret() {
        stringGenerator sg = new stringGenerator();
        return sg.generateString(75);
    }

    public Boolean validateClient(String clientId, String clientSecret) {
        try {
            String selectString = "SELECT count(*) as count FROM oauthClients WHERE client_id = ? AND client_secret = ?";
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

    public Boolean validateCode(String clientID, String code) {
        try {
            String selectString = "SELECT ts FROM oauthNonces WHERE client_id = ? AND code = ?";
            PreparedStatement stmt = conn.prepareStatement(selectString);

            stmt.setString(1, clientID);
            stmt.setString(2, code);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("ts");
                // get a Timestamp instance for 10 mins ago
                Timestamp expiredTs = new Timestamp(Calendar.getInstance().getTime().getTime() - 600000);
                // if ts is older then 10 mins, we can't proceed
                if (ts == null || ts.before(expiredTs)) {
                    return false;
                }

                // code's are only good for 1 use, delete entry from db
                String deleteString = "DELETE FROM oauthNonces WHERE client_id = ? AND code = ?";
                PreparedStatement stmt2 = conn.prepareStatement(deleteString);

                stmt2.setString(1, clientID);
                stmt2.setString(2, code);

                stmt2.execute();

                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String generateToken(String clientID, String state) throws SQLException{
        stringGenerator sg = new stringGenerator();
        String token = sg.generateString(20);

        String insertString = "INSERT INTO oauthTokens (client_id, token) VALUE (?, \"" + token +"\")";
        PreparedStatement stmt = conn.prepareStatement(insertString);

        stmt.setString(1, clientID);
        stmt.execute();

        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        sb.append("\"access_token\":\"" + token + "\",\n");
        sb.append("\"token_type\":\"bearer\",\n");
        sb.append("\"expires_in\":3600\n");
        if (state != null) {
            sb.append(("\"state\":\"" + state + "\""));
        }
        sb.append("}]");

        return sb.toString();
    }

    /**
     * given a hostname, register a client app for oauth use
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

            String insertString = "INSERT INTO oauthClients (client_id, client_secret, callback) VALUES (\""
                                  + clientId + "\",\"" + clientSecret + "\",?)";
            PreparedStatement stmt = p.conn.prepareStatement(insertString);

            stmt.setString(1, host);
            stmt.execute();

            System.out.println("Successfully registered oauth2 client app at host: " + host
                    + ".\nYou will need the following information:\n\nclient_id: "
                    + clientId + "\nclientSecret: " + clientSecret);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
