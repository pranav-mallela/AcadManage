package org.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private final String url = "jdbc:postgresql://localhost/acadmanage";
    private final String user = "acadadmin";
    private final String password = "adminpass";

    public Connection connect()
    {
        Connection c = null;
        try {
            c = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return c;
    }
}
