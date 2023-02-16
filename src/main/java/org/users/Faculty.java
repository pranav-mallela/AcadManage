package org.users;

import java.io.File;
import java.sql.*;
import java.util.Scanner;

public class Faculty extends User{
    private final int facultyId;
    private String dept;

    public Faculty(Connection conn, int facultyId) {
        super(conn);
        this.facultyId = facultyId;
    }

    public void floatCourse(String courseCode, int year, int semester)
    {
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement;
        ResultSet rs;

        // check if matching with upcoming year and sem and add it to offerings
        try{
            if(checkIfUpcomingSem(year, semester))
            {
                String offeringQuery = String.format("INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in)" +
                        "VALUES(%d, %d, %d, %d)", facultyId, courseId, year, semester);
                statement = conn.createStatement();
                statement.executeUpdate(offeringQuery);
                System.out.println("SUCCESS: Course successfully floated!");
            }
            else
            {
                System.out.println("ERROR: Cannot float due to wrong year or semester!");
                return;
            }
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void addConstraintsToOffering(int year, int semester, String courseCode)
    {
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;


    }

    public void cancelOffering(String courseCode, int year, int semester)
    {
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement, statement1;
        ResultSet rs;

        try{
            // only allow if offering is in upcoming semester
            if(!checkIfUpcomingSem(year, semester))
            {
                System.out.println("ERROR: Cannot cancel offering that is not in upcoming semester!");
                return;
            }

            // delete offering from student table
            String enrolledStudentsQuery = String.format("SELECT * FROM offering_%d", offeringId);
            statement = conn.createStatement();
            rs = statement.executeQuery(enrolledStudentsQuery);
            while(rs.next())
            {
                String cancelStatusQuery = String.format("DELETE FROM student_%d WHERE offering_id=%d",
                        rs.getInt("student_id"), offeringId);
                statement1 = conn.createStatement();
                statement1.executeUpdate(cancelStatusQuery);
            }

            // delete offering
            String cancelOfferingQuery  = String.format("DELETE FROM offerings WHERE course_id=%d and faculty_id=%d and year_offered_in=%d and semester_offered_in=%d", courseId, facultyId, year, semester);
            statement = conn.createStatement();
            statement.executeUpdate(cancelOfferingQuery);
            System.out.println("SUCCESS: Offering cancelled successfully!");
        } catch(Exception e)
        {
            System.out.println(e);
        }

    }

    public void uploadGrades(String courseCode, int year, int semester)
    {
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement,statement1, statement2;
        ResultSet rs = null, rs1 = null;
        Scanner s = new Scanner(System.in);

        try{
            // export students table to faculty computer
            String dir = String.format("C:/Users/Public/Grades_%d", courseId);
            new File(dir).mkdirs();
            String exportCSVQuery = String.format("COPY offering_%d TO '%s/offering_%d.csv' CSV HEADER", offeringId, dir, offeringId);
            statement = conn.createStatement();
            statement.executeUpdate(exportCSVQuery);
            System.out.println("\nCheck the directory C:/Users/Public/Grades_<courseID>/offering_<offeringID>.csv for the csv file containing enrolled students' information.");

            // import csv from path given by faculty
            System.out.print("Press enter once all the grades have been updated and the file has been saved: ");
            s.nextLine();
            String createTempTableQuery = String.format("CREATE TABLE offering_tmp_%d(" +
                    "student_id INT," +
                    "grade VARCHAR(10)," +
                    "PRIMARY KEY(student_id)" +
                    ")", offeringId);
            statement.executeUpdate(createTempTableQuery);
            String importCSVQuery = String.format("COPY offering_tmp_%d FROM '%s/offering_%d.csv' DELIMITER ',' CSV HEADER", offeringId, dir, offeringId);
            statement.executeUpdate(importCSVQuery);

            // ERROR checking
            // if all students are in the csv file
            String checkStudents = String.format("(SELECT student_id FROM offering_%d EXCEPT SELECT student_id FROM offering_tmp_%d)" +
                    "UNION" +
                    "(SELECT student_id FROM offering_tmp_%d EXCEPT SELECT student_id FROM offering_%d)", offeringId, offeringId, offeringId, offeringId);
            rs = statement.executeQuery(checkStudents);
            if(rs.next())
            {
                System.out.println("ERROR: Student mismatch!");
                String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
                statement.executeUpdate(deleteTempTableQuery);
                return;
            }

            //check if all students have been given grades
            String checkGrades = String.format("SELECT grade FROM offering_tmp_%d WHERE grade IS NULL OR (grade <> 'A' and grade <> 'A-' and grade <> 'B' and grade <> 'B-' and grade <> 'C' and grade <> 'C-' and grade <> 'D' and grade <> 'D-'" +
                    "and grade <> 'E' and grade <> 'E-' and grade <> 'F')", offeringId);
            rs = statement.executeQuery(checkGrades);
            if(rs.next())
            {
                System.out.println("ERROR: Grades can only be of a specific format!");
                String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
                statement.executeUpdate(deleteTempTableQuery);
                return;
            }

            //update student grades in the offering table
            String getEnrolledStudents = String.format("SELECT * FROM offering_%d", offeringId);
            rs = statement.executeQuery(getEnrolledStudents);
            while(rs.next())
            {
                statement1 = conn.createStatement();
                String getGradeFromTempQuery = String.format("SELECT * FROM offering_tmp_%d WHERE student_id='%d'", offeringId, rs.getInt("student_id"));
                rs1 = statement1.executeQuery(getGradeFromTempQuery);
                if(rs1.next())
                {
                    String updateOfferingTableQuery = String.format("UPDATE offering_%d SET grade='%s' WHERE student_id=%d", offeringId, rs1.getString("grade"), rs.getInt("student_id"));
                    statement2 = conn.createStatement();
                    statement2.executeUpdate(updateOfferingTableQuery);
                }
                else
                {
                    System.out.println("ERROR: Invalid student ID!");
                    return;
                }
            }

            //update student grades in the students' table
            rs = statement.executeQuery(getEnrolledStudents);
            while(rs.next())
            {
                statement1 = conn.createStatement();
                String getGradeFromTempQuery = String.format("SELECT * FROM offering_tmp_%d WHERE student_id='%d'", offeringId, rs.getInt("student_id"));
                rs1 = statement1.executeQuery(getGradeFromTempQuery);
                if(rs1.next())
                {
                    String updateStudentTableQuery = String.format("UPDATE student_%d SET grade='%s' WHERE offering_id=%d", rs.getInt("student_id"), rs1.getString("grade"), offeringId);
                    statement2 = conn.createStatement();
                    statement2.executeUpdate(updateStudentTableQuery);
                }
                else
                {
                    System.out.println("ERROR: Invalid student ID!");
                    return;
                }
            }

            // delete temp offering table
            String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
            statement.executeUpdate(deleteTempTableQuery);

            System.out.println("SUCCESS: Grades have been successfully updated!");

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public void viewGrades(int year, int semester, String courseCode)
    {
        Statement statement;
        ResultSet rs = null;

        try{
            // get courseId and offeringId
            int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
            int courseId = idArray[0], offeringId = idArray[1];
            if(courseId == 0 || offeringId == 0) return;

            String viewGradesQuery = String.format("SELECT * FROM offering_%d", offeringId);
            statement = conn.createStatement();
            rs = statement.executeQuery(viewGradesQuery);
            System.out.println(" student_id | grade");
            System.out.println("------------+--------");
            while(rs.next())
            {
                System.out.print(" ".repeat("student_id".length()) + rs.getInt("student_id") + " | "
                        + rs.getString("grade") + "\n");
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

}
