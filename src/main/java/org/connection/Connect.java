package org.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private final String url;
    private final String user;
    private final String password;

    public Connect(String db, String user, String pass) {
        this.url = "jdbc:postgresql://localhost/" + db;
        this.user = user;
        this.password = pass;
    }

    public Connection connect() throws SQLException
    {
        Connection c = null;
        c = DriverManager.getConnection(url, user, password);
        System.out.print("Connected to the PostgreSQL server successfully.\n");
        return c;
    }
}
