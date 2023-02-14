package org.users;

import javax.swing.plaf.nimbus.State;
import java.io.File;
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

    public void generateTranscript(int studentId, int year, int semester)
    {
        Statement statement;
        ResultSet rs = null;

        try{
            // query to get the transcript of a student
            String getCourseIdQuery = String.format("SELECT course_id FROM course_catalog WHERE course_code=s.course_code");
            String getOfferingIdQuery = String.format("SELECT offering_id FROM offerings WHERE year_offered_in=%d and semester_offered_in=%d and course_id=(%s)", year, semester, getCourseIdQuery);
            String getStudentTranscriptQuery = String.format("SELECT course_code, grade FROM student_%d s WHERE offering_id=(%s)", studentId, getOfferingIdQuery);

            // export txt file to user's computer
            //TODO: styling txt file
            statement = conn.createStatement();
            String pathToTranscript = String.format("C:/Users/Public/Transcripts/Student_%d", studentId);
            new File(pathToTranscript).mkdirs();
            String exportTranscriptQuery = String.format("COPY (%s) TO '%s/transcript_%d_%d.txt'", getStudentTranscriptQuery, pathToTranscript, year, semester);
            statement.executeUpdate(exportTranscriptQuery);
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public boolean gradCheck(String rollNo)
    {
        // wish life were this easy
        return true;
    }
}
