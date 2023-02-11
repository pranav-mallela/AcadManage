package org.users;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class AcadOffice extends User{
    public AcadOffice(Connection conn) {
        super(conn);
    }

    public void addCourseToCatalog(String courseCode, int l, int t, int p)
    {
        Statement statement;
        ResultSet rs;
        // check if course already exists
        int courseId = checkIfCourseExists(courseCode);
        if(courseId != 0)
        {
            System.out.println("Course already exists!");
            return;
        }

        //add course
        try{
            String addCourseQuery = String.format("INSERT INTO course_catalog(course_code, l, t, p) VALUES('%s', %d, %d, %d);", courseCode, l, t, p);
            statement = conn.createStatement();
            statement.executeUpdate(addCourseQuery);
            System.out.println("Course successfully added to catalog!");
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public void generateTranscript(String rollNo, int year, int semester)
    {
        // print transcript
    }

    public boolean gradCheck(String rollNo)
    {
        // wish life were this easy
        return true;
    }
}
