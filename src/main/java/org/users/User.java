package org.users;

import java.sql.*;

public class User {
    private String name;
    private String username;
    private String phone;
    private String password;
    protected Connection conn;

    public User(Connection conn)
    {
        this.conn = conn;
    }
    public int login(String username, String password, String role)
    {
        Statement statement;
        ResultSet rs = null;
        try{
            String query="";
            if(role.equals("0"))
            {
                query = String.format("select * from student_credentials where username='%s' and pass='%s'", username, password);
            }
            else if(role.equals("1"))
            {
                query = String.format("select * from faculty_credentials where username='%s' and pass='%s'", username, password);
            }
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while(rs.next())
            {
                return rs.getInt("id");
            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
        return -1;
    }

    public void resetPass(String email)
    {
        //reset password
    }

    public void editProfile()
    {
        // edit profile
    }
}
