package bcid;

import auth.oauth2.provider;

import java.sql.*;

/**
 * Get an html table of the user's profile
 * Created by rjewing on 1/24/14.
 */
public class profileRetriever {
    protected Connection conn;

    public profileRetriever() throws Exception {
        database db = new database();
        conn = db.getConn();
    }




}
