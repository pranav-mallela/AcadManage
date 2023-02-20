package org.connection;
import org.users.AcadOffice;
import org.users.Faculty;
import org.users.Student;
import org.users.User;

import java.sql.Connection;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // connection
        Connect portal = new Connect();
        Connection conn = portal.connect();

        // get credentials and assign role
        UI ui = new UI();
        while(true)
        {
            ArrayList<String> credentials = ui.getCredentials();
            String role = credentials.get(2);
            User user = new User(conn);
            int id = user.login(credentials.get(0), credentials.get(1), role);
            if(id == -1)
            {
                System.out.println("Invalid credentials!");
                return;
            }

            // present menu depending on role
            switch (role) {
                case "0" -> {
                    user = new Student(conn, id);
                    ui.studentMenu((Student) user);
                }
                case "1" -> {
                    user = new Faculty(conn, id);
                    ui.facultyMenu((Faculty) user);
                }
                case "2" -> {
                    user = new AcadOffice(conn);
                    ui.acadMenu((AcadOffice) user);
                }
            }
        }
    }
}