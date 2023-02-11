package org.users;

import java.sql.*;

public class Student extends User{
    private final int studentId;
    private int entry_year;

    public Student(Connection conn, int studentId) {
        super(conn);
        this.studentId = studentId;
    }

    public void addCourse(String courseCode, int year, int semester)
    {
        Statement statement;
        ResultSet rs = null;

        // get the student's entry year
        try{
            String yearQuery = String.format("select * from students where student_id=%d;", this.studentId);
            statement = conn.createStatement();
            rs = statement.executeQuery(yearQuery);
            while(rs.next()) {
                this.entry_year = rs.getInt("entry_year");
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }

        // check if the course exists
        int courseId = checkIfCourseExists(courseCode);
        if(courseId == 0)
        {
            System.out.println("Course does not exist!");
            return;
        }

        // check if the course is offered
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId == 0)
        {
            System.out.println("Course not offered in the given year and semester!");
            return;
        }

        //TODO: enforce credit limit
        //TODO: check if meeting pre-req

        // get the current academic year and semester, and see if everything is ok
        try{
            String semQuery = "select * from upcoming_semester where upcoming=(1::boolean);";
            statement = conn.createStatement();
            rs = statement.executeQuery(semQuery);
            while(rs.next())
            {
                int acad_year = rs.getInt("academic_year");
                if(acad_year == year && rs.getInt("semester") == semester && (acad_year - entry_year) < 4)
                {
                    String addQuery = String.format("INSERT INTO student_%d" +
                            "(offering_id, course_code, status)" +
                            " VALUES(%d, '%s', 'EN');", studentId, offeringId, courseCode);
                    statement = conn.createStatement();
                    statement.executeUpdate(addQuery);
                    String addToOfferingQuery = String.format("INSERT INTO offering_%d VALUES(%s)", offeringId, studentId);
                    statement.executeUpdate(addToOfferingQuery);
                    System.out.println("Course successfully enrolled!");
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
    }

    public void dropCourse(String courseCode, int year, int semester)
    {
        // drop course
    }

    public void viewGrades(int year, int semester)
    {
        // view grades
    }
    public void CGPA(String rollNo)
    {
        // CGPA
    }
    public void SGPA(String rollNo, int semester)
    {
        // SGPA
    }
}
