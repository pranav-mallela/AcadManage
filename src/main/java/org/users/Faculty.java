package org.users;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

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

        // check if matching with upcoming year and sem and add it to offerings
        try{
            if(checkIfUpcomingSem(year, semester))
            {
                String offeringQuery = String.format("INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in)" +
                        "VALUES(%d, %d, %d, %d)", facultyId, courseId, year, semester);
                statement = conn.createStatement();
                statement.executeUpdate(offeringQuery);
                System.out.println("Course successfully floated!");
            }
            else
            {
                System.out.println("Cannot float due to wrong year or semester!");
                return;
            }
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void cancelOffering(String courseCode, int year, int semester)
    {
        int courseId = checkIfCourseExists(courseCode);
        if(courseId == 0)
        {
            System.out.println("Course does not exist!");
            return;
        }
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId == 0)
        {
            System.out.println("Offering does not exist!");
            return;
        }

        Statement statement, statement1;
        ResultSet rs;

        try{
            // only allow if offering is in upcoming semester
            if(!checkIfUpcomingSem(year, semester))
            {
                System.out.println("Cannot cancel offering that is not in upcoming semester!");
                return;
            }

            // change student status to CNCL
            String enrolledStudentsQuery = String.format("SELECT * FROM offering_%d", offeringId);
            statement = conn.createStatement();
            rs = statement.executeQuery(enrolledStudentsQuery);
            while(rs.next())
            {
                String cancelStatusQuery = String.format("UPDATE student_%d SET status='CNCL' WHERE offering_id=%d",
                        rs.getInt("student_id"), offeringId);
                statement1 = conn.createStatement();
                statement1.executeUpdate(cancelStatusQuery);
            }

            // delete offering
            String cancelOfferingQuery  = String.format("DELETE FROM offerings WHERE course_id=%d and faculty_id=%d and year_offered_in=%d and semester_offered_in=%d", courseId, facultyId, year, semester);
            statement = conn.createStatement();
            statement.executeUpdate(cancelOfferingQuery);
            System.out.println("Offering cancelled successfully!");
        } catch(Exception e)
        {
            System.out.println(e);
        }

    }

    public void uploadGrades(String courseCode, int year, int semester)
    {
        int courseId = checkIfCourseExists(courseCode);
        if(courseId == 0)
        {
            System.out.println("Course does not exist!");
            return;
        }
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId == 0)
        {
            System.out.println("Offering does not exist!");
            return;
        }

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
                    "grade VARCHAR(2)," +
                    "PRIMARY KEY(student_id)" +
                    ")", offeringId);
            statement.executeUpdate(createTempTableQuery);
            String importCSVQuery = String.format("COPY offering_tmp_%d FROM '%s/offering_%d.csv' DELIMITER ',' CSV HEADER", offeringId, dir, offeringId);
            statement.executeUpdate(importCSVQuery);

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
                    System.out.println("Invalid student ID entered!");
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
                    System.out.println("Invalid student ID entered!");
                    return;
                }
            }

            // delete temp offering table
            String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
            statement.executeUpdate(deleteTempTableQuery);

            System.out.println("Grades have been successfully updated!");

        } catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
