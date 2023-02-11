package org.users;

import java.sql.*;

public class Faculty extends User{
    private int facultyId;
    private String dept;

    public Faculty(Connection conn, int facultyId) {
        super(conn);
        this.facultyId = facultyId;
    }

    public void floatCourse(String courseCode, int year, int semester)
    {
        Statement statement;
        ResultSet rs;
        // check if course is in catalog
        int courseId = checkIfCourseExists(courseCode);
        if(courseId == 0)
        {
            System.out.println("Course does not exist!");
            return;
        }

        // check if course has already been offered in that year and sem
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId != 0)
        {
            System.out.println("Offering already exists!");
            return;
        }

        // check if matching with upcoming year and sem
        try{
            String semQuery = "select * from upcoming_semester where upcoming=(1::boolean);";
            statement = conn.createStatement();
            rs = statement.executeQuery(semQuery);
            while(rs.next())
            {
                int acad_year = rs.getInt("academic_year");
                if(acad_year == year && rs.getInt("semester") == semester)
                {
                    String offeringQuery = String.format("INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in)" +
                            "VALUES(%d, %d, %d, %d)", facultyId, courseId, year, semester);
                    statement = conn.createStatement();
                    statement.executeUpdate(offeringQuery);
                    System.out.println("Course successfully floated!");
                }
                else
                {
                    System.out.println("Cannot enroll due to wrong year or semester!");
                    return;
                }
            }
        } catch(Exception e) {
            System.out.println(e);
        }

        // add to offering table

    }

    public void cancelOffering(String courseCode, int year, int semester)
    {
        // cancel offering
    }

    public void uploadGrades(String courseCode, int year, int semester)
    {
        // upload grades
    }
}
