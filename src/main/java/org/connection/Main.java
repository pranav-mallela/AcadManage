package org.connection;
import org.scanners.CustomScanner;
import org.users.AcadOffice;
import org.users.Faculty;
import org.users.Student;
import org.users.User;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Connect portal = new Connect("acadmanage", "acadadmin", "adminpass");
            Connection conn = portal.connect();

            // get credentials and assign role
            UI ui = new UI();
            CustomScanner s = new CustomScanner();
            while (true) {
                ArrayList<String> credentials = ui.getCredentials(s);
                String role = credentials.get(2);
                User user = new User(conn);
                int id = user.login(credentials.get(0), credentials.get(1), role);
                if (id == -1) {
                    System.out.print("UNSUCCESSFUL LOGIN: Invalid credentials!\n");
                    System.out.print("Do you want to continue? (Y/N): ");
                    if(s.getString().equals("N")) break;
                    continue;
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
        } catch (Exception e) {
            System.out.print(e);
        }
    }
}