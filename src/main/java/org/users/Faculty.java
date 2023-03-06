package org.users;

import org.scanners.CustomScanner;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class Faculty extends User{
    public final int facultyId;
    private String dept;

    public Faculty(Connection conn, int facultyId) {
        super(conn);
        this.facultyId = facultyId;
    }

    // can only be done 'Before Semester'
    public void floatCourse(String courseCode, int year, int semester) throws SQLException
    {
        if(!getRunningPhase(1)) return;

        int courseId = checkIfCourseExists(courseCode, true);
        if(courseId == 0) return;

        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(offeringId != 0)
        {
            System.out.print("UNSUCCESSFUL ACTION: Offering already exists!\n");
            return;
        }

        Statement statement;
        ResultSet rs;

        // check if matching with upcoming year and sem and add it to offerings
        if(checkIfUpcomingSem(year, semester))
        {
            String offeringQuery = String.format("INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in)" +
                    "VALUES(%d, %d, %d, %d)", facultyId, courseId, year, semester);
            statement = conn.createStatement();
            statement.executeUpdate(offeringQuery);
            System.out.print("SUCCESS: Course successfully floated!\n");
        }
        else
        {
            System.out.print("UNSUCCESSFUL ACTION: Cannot float due to wrong year or semester!\n");
        }
    }

    // can only be done 'Before Semester'
    public void addConstraintsToOffering(int year, int semester, String courseCode, List<List<List<String>>> orPreReqGrades) throws  SQLException
    {
        if(!getRunningPhase(1)) return;

        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement;

        for (List<List<String>> orPreReqGrade : orPreReqGrades) {
            int mainPreReqId = checkIfCourseExists(orPreReqGrade.get(orPreReqGrade.size() - 1).get(0), true);
            if (mainPreReqId == 0) {
                String deleteConstraintsQuery = String.format("DELETE FROM offering_constraints WHERE offering_id=%d", offeringId);
                statement = conn.createStatement();
                statement.executeUpdate(deleteConstraintsQuery);
                return;
            }
            String mainPreReqGrade = orPreReqGrade.get(orPreReqGrade.size() - 1).get(1);

            String addMainConstraintsQuery = String.format("INSERT INTO offering_constraints VALUES(%d, %d, '%s')", offeringId, mainPreReqId, mainPreReqGrade);
            statement = conn.createStatement();
            statement.executeUpdate(addMainConstraintsQuery);

            for (int j = 0; j < orPreReqGrade.size() - 1; j++) {
                String preReqCode = orPreReqGrade.get(j).get(0);
                String preReqGrade = orPreReqGrade.get(j).get(1);
                int preReqId = checkIfCourseExists(preReqCode, true);
                if (preReqId == 0) {
                    String deleteConstraintsQuery = String.format("DELETE FROM optional_offering_constraints WHERE offering_id=%d", offeringId);
                    statement = conn.createStatement();
                    statement.executeUpdate(deleteConstraintsQuery);
                    return;
                }
                String addConstraintsQuery = String.format("INSERT INTO optional_offering_constraints VALUES(%d, %d, %d, '%s')", offeringId, mainPreReqId, preReqId, preReqGrade);
                statement = conn.createStatement();
                statement.executeUpdate(addConstraintsQuery);
            }
        }
        System.out.print("SUCCESS: Constraints successfully added!\n");
    }

    // can only be done 'Before Semester'
    public void addCGConstraints(int year, int semester, String courseCode, float cg) throws SQLException
    {
        if(!getRunningPhase(1)) return;

        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement;

        String addCGConstraintsQuery = String.format("INSERT INTO offering_cg_constraints VALUES(%d, %f)", offeringId, cg);
        statement = conn.createStatement();
        statement.executeUpdate(addCGConstraintsQuery);
        System.out.print("SUCCESS: CG Constraints successfully added!\n");
    }

    // can only be done 'Before Semester'
    public void cancelOffering(String courseCode, int year, int semester) throws SQLException
    {
        if(!getRunningPhase(1)) return;

        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement, statement1;
        ResultSet rs;

        // only allow if offering is in upcoming semester
        if(!checkIfUpcomingSem(year, semester))
        {
            System.out.print("UNSUCCESSFUL ACTION: Cannot cancel offering that is not in upcoming semester!\n");
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
        System.out.print("SUCCESS: Offering cancelled successfully!\n");
    }

    // can only be done 'After Semester'
    public void uploadGrades(String courseCode, int year, int semester, CustomScanner s) throws SQLException
    {
        if(!getRunningPhase(3)) return;

        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        Statement statement,statement1, statement2;
        ResultSet rs = null, rs1 = null;

        try{
            // export students table to faculty computer
            String dir = String.format("C:/Users/Public/Grades_%d", courseId);
            new File(dir).mkdirs();
            String exportCSVQuery = String.format("COPY offering_%d TO '%s/offering_%d.csv' CSV HEADER", offeringId, dir, offeringId);
            statement = conn.createStatement();
            statement.executeUpdate(exportCSVQuery);
            System.out.print("\nCheck the directory C:/Users/Public/Grades_<courseID>/offering_<offeringID>.csv for the csv file containing enrolled students' information.\n");

            // import csv from path given by faculty
            System.out.println("Press enter once all the grades have been updated and the file has been saved: ");
            s.getString();
            String createTempTableQuery = String.format("CREATE TABLE offering_tmp_%d(student_id INT,grade VARCHAR(10),PRIMARY KEY(student_id))", offeringId);
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
                System.out.print("UNSUCCESSFUL ACTION: Student mismatch!\n");
                String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
                statement.executeUpdate(deleteTempTableQuery);
                return;
            }

            //check if all students have been given valid grades (null grade is allowed)
            String checkGrades = String.format("SELECT grade FROM offering_tmp_%d WHERE grade IS NOT NULL AND (grade <> 'A' and grade <> 'A-' and grade <> 'B' and grade <> 'B-' and grade <> 'C' and grade <> 'C-' and grade <> 'D' and grade <> 'D-'" +
                    "and grade <> 'E' and grade <> 'E-' and grade <> 'F')", offeringId);
            rs = statement.executeQuery(checkGrades);
            if(rs.next())
            {
                System.out.print("UNSUCCESSFUL ACTION: Grades can only be of a specific format!\n");
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
                    String grade = rs1.getString("grade");
                    if(grade == null)
                        grade = "";
                    String updateOfferingTableQuery = String.format("UPDATE offering_%d SET grade='%s' WHERE student_id=%d", offeringId, grade, rs.getInt("student_id"));
                    statement2 = conn.createStatement();
                    statement2.executeUpdate(updateOfferingTableQuery);
                }
                else
                {
                    System.out.print("UNSUCCESSFUL ACTION: Invalid student ID!\n");
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
                    String grade = rs1.getString("grade");
                    if(grade == null)
                        grade = "";
                    String updateStudentTableQuery = String.format("UPDATE student_%d SET grade='%s' WHERE offering_id=%d", rs.getInt("student_id"), grade, offeringId);
                    statement2 = conn.createStatement();
                    statement2.executeUpdate(updateStudentTableQuery);
                }
                else
                {
                    System.out.print("UNSUCCESSFUL ACTION: Invalid student ID!\n");
                    return;
                }
            }

            // delete temp offering table
            String deleteTempTableQuery = String.format("DROP TABLE offering_tmp_%d", offeringId);
            statement.executeUpdate(deleteTempTableQuery);

            System.out.print("SUCCESS: Grades have been successfully updated!\n");

        } catch (SQLException e)
        {
            System.out.print(e);
        }
    }

    public void viewGrades(int year, int semester, String courseCode) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;

        // get courseId and offeringId
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        String viewGradesQuery = String.format("SELECT * FROM offering_%d", offeringId);
        statement = conn.createStatement();
        rs = statement.executeQuery(viewGradesQuery);
        System.out.print(" student_id | grade\n");
        System.out.print("------------+--------\n");
        while(rs.next())
        {
            System.out.print(" ".repeat("student_id".length()) + rs.getInt("student_id") + " | "
                    + rs.getString("grade") + "\n");
        }
    }

}
