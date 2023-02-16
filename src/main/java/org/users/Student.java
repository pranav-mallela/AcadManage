package org.users;

import javax.swing.plaf.nimbus.State;
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
            System.out.println("ERROR: Course does not exist!");
            return;
        }

        // check if the course is offered
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId == 0)
        {
            System.out.println("ERROR: Course not offered in the given year and semester!");
            return;
        }

        // TODO: enforce credit limit
        // TODO: check if meeting pre-req

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
                    System.out.println("ERROR: Course successfully enrolled!");
                }
                else
                {
                    System.out.println("ERROR: Cannot enroll due to wrong year or semester!");
                    return;
                }
            }
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void dropCourse(String courseCode)
    {
        Statement statement;
        ResultSet rs = null;

        try{
           // check if the course is in the corresponding student table, if so, drop it and remove the student from the offering
           String existsInEnrollmentsQuery = String.format("SELECT * FROM student_%d WHERE course_code='%s' and grade is NULL", studentId, courseCode);
           statement = conn.createStatement();
           rs = statement.executeQuery(existsInEnrollmentsQuery);
           if(rs.next())
           {
               int offeringId = rs.getInt("offering_id");
               String dropCourseQuery = String.format("DELETE FROM student_%d WHERE course_code='%s' and grade IS NULL", studentId, courseCode);
               String removeStudentQuery = String.format("DELETE FROM offering_%d WHERE student_id=%d", offeringId, studentId);
               statement = conn.createStatement();
               statement.executeUpdate(dropCourseQuery);
               statement.executeUpdate(removeStudentQuery);
               System.out.println("SUCCESS: Course successfully dropped!");
           }
           else
           {
               System.out.println("ERROR: Enrollment does not exist! Cannot drop course!");
           }

        } catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void viewEnrolledCourseDetails()
    {
        Statement statement;
        ResultSet rs = null;

        try{
            String viewQuery = String.format("SELECT * FROM student_%d", studentId);
            statement = conn.createStatement();
            rs = statement.executeQuery(viewQuery);
            System.out.println(" offering_id | course_code | status | grade");
            System.out.println("-------------+-------------+--------+--------");
            while(rs.next())
            {
                System.out.print(" ".repeat("offering_id".length()) + rs.getInt("offering_id") + " | "
                        + rs.getString("course_code") + " ".repeat("course_code".length()-4) + "| "
                        + rs.getString("status") + " ".repeat("status".length()-1) + "| "
                        + rs.getString("grade") + "\n");
            }
        } catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public float CGPA()
    {
        Statement statement, statement1;
        ResultSet rs, rs1 = null;
        int cgpa = 0;
        int totalCredits = 0;

        try {
            String getGradesQuery = String.format("SELECT course_code, grade FROM student_%d WHERE grade IS NOT NULL", studentId);
            statement = conn.createStatement();
            rs = statement.executeQuery(getGradesQuery);
            while(rs.next())
            {
                String getCourseCredits = String.format("SELECT c FROM course_catalog WHERE course_code='%s'", rs.getString("course_code"));
                statement1 = conn.createStatement();
                rs1 = statement1.executeQuery(getCourseCredits);
                int credits = 0;
                if(rs1.next())
                {
                    credits = rs1.getInt("c");
                };
                totalCredits += credits;
                String grade = rs.getString("grade");
                cgpa = switch (grade) {
                    case "A" -> cgpa + credits * 10;
                    case "A-" -> cgpa + credits * 9;
                    case "B" -> cgpa + credits * 8;
                    case "B-" -> cgpa + credits * 7;
                    case "C" -> cgpa + credits * 6;
                    case "C-" -> cgpa + credits * 5;
                    case "D" -> cgpa + credits * 4;
                    case "D-" -> cgpa + credits * 3;
                    case "E" -> cgpa + credits * 2;
                    case "E-" -> cgpa + credits;
                    default -> cgpa;
                };
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        if(totalCredits == 0)
        {
            return 0;
        }
        return cgpa/totalCredits;
    }
    public void SGPA(String rollNo, int semester)
    {
        // SGPA
    }
}
