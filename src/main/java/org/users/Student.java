package org.users;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.concurrent.ExecutionException;

public class Student extends User{
    private final int studentId;
    private int entry_year;

    public Student(Connection conn, int studentId) {
        super(conn);
        this.studentId = studentId;
    }

    // can only be done 'Before Semester'
    public void addCourse(String courseCode, int year, int semester)
    {
        if(!getRunningPhase(1)) return;

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

        // check if the course and offering exist
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        // TODO: enforce credit limit

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
                    // checking pre-reqs and constraints, including cgpa
                    if(!isPassingConstraints(offeringId) || !isPassingPreReqs(courseCode) || !isPassingCGCriteria(offeringId)) return;

                    String addQuery = String.format("INSERT INTO student_%d" +
                            "(offering_id, course_code, status)" +
                            " VALUES(%d, '%s', 'EN');", studentId, offeringId, courseCode);
                    statement = conn.createStatement();
                    statement.executeUpdate(addQuery);
                    String addToOfferingQuery = String.format("INSERT INTO offering_%d VALUES(%s)", offeringId, studentId);
                    statement.executeUpdate(addToOfferingQuery);
                    System.out.println("SUCCESS: Course successfully enrolled!");
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

    // get the cgpa of the student and min cgpa of the offering
    // then return true if the student's cgpa is greater than or equal to the min cgpa
    private boolean isPassingCGCriteria(int offeringId)
    {
        Statement statement;
        ResultSet rs = null;

        try{
            String getCGConstraintsQuery = String.format("select * from offering_cg_constraints where offering_id=%d", offeringId);
            statement = conn.createStatement();
            rs = statement.executeQuery(getCGConstraintsQuery);
            if(!rs.next()) return true;
            else
            {
                int min_cgpa = rs.getInt("cg");
                float studentCG = CGPA();
                if(studentCG >= min_cgpa) return true;
                else
                {
                    System.out.println("ERROR: CGPA is less than the minimum required!");
                    return false;
                }
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return false;
    }

    private boolean isPassingPreReqs(String courseCode)
    {
        Statement statement, statement1;
        ResultSet rs = null, rs1 = null;
        boolean passedPreReqs = false;

        try {
            // if no prereqs exist, mark true
            rs = conn.createStatement().executeQuery(String.format("SELECT pre_req_course_id FROM pre_req WHERE course_code='%s'", courseCode));
            if(!rs.next()) return true;

            // get grades of the student in the acad office set pre reqs
            String getGradesQuery = String.format("SELECT s.course_code, s.grade FROM student_%d s, pre_req p WHERE p.course_code='%s' and p.pre_req_code=s.course_code", studentId, courseCode);
            statement = conn.createStatement();
            rs = statement.executeQuery(getGradesQuery);
            while(rs.next())
            {
                // if any grade is less than D, check in the optional pre reqs
                if(rs.getString("grade").compareTo("D") > 0)
                {
                    String checkOptionalPreReqQuery = String.format("select grade from student_%d s, optional_pre_req o where s.course_code=o.option_code and o.pre_req_code='%s'", studentId, rs.getString("course_code"));
                    statement1 = conn.createStatement();
                    rs1 = statement1.executeQuery(checkOptionalPreReqQuery);
                    boolean passedThisPreReq = false;
                    while(rs1.next())
                    {
                        if(rs1.getString("grade").compareTo("D") <= 0)
                        {
                            passedThisPreReq = true;
                        }
                    }
                    if(!passedThisPreReq)
                    {
                        System.out.println("ERROR: You have not cleared the prerequisites for this course!");
                        return false;
                    }
                }
                passedPreReqs = true;
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        if(!passedPreReqs)
        {
            System.out.println("ERROR: You have not cleared the prerequisites for this course!");
        }
        return passedPreReqs;
    }

    private boolean isPassingConstraints(int offeringId)
    {
        Statement statement, statement1;
        ResultSet rs = null, rs1 = null, rs2 = null;

        try {
            //if no constraints exist, mark as true
            rs = conn.createStatement().executeQuery(String.format("SELECT course_id FROM offering_constraints WHERE offering_id=%d", offeringId));
            if(!rs.next()) return true;

            // get courses set by the instructor as constraints
            String getConstraintCoursesQuery = String.format("SELECT course_id, grade from offering_constraints WHERE offering_id=%d", offeringId);
            statement = conn.createStatement();
            rs = statement.executeQuery(getConstraintCoursesQuery);
            while (rs.next())
            {
                // get the grades of the student in the corresponding courses
                int courseId = rs.getInt("course_id");
                String getGradesQuery = String.format("SELECT grade FROM student_%d WHERE course_code=(SELECT course_code FROM course_catalog WHERE course_id=%d)", studentId, courseId);
                statement1 = conn.createStatement();
                rs1 = statement1.executeQuery(getGradesQuery);
                if (rs1.next())
                {
                    // if a constraint is not met, go into the corresponding optional offering constraints
                    if (rs1.getString("grade").compareTo(rs.getString("grade")) > 0)
                    {
                        String checkOptionalConstraintsQuery = String.format("SELECT option_course_id, grade FROM optional_offering_constraints WHERE course_id=%d", courseId);
                        rs2 = conn.createStatement().executeQuery(checkOptionalConstraintsQuery);
                        boolean passedThisConstraint = false;
                        while(rs2.next())
                        {
                            if(rs2.getString("grade").compareTo(rs1.getString("grade")) <= 0)
                            {
                                passedThisConstraint = true;
                            }
                        }
                        // if all optional offerings fail, return ERROR
                        if(!passedThisConstraint)
                        {
                            System.out.println("ERROR: Constraints not passed!");
                            return false;
                        }
                    }
                }
                else
                {
                    System.out.println("ERROR: Constraints not passed!");
                    return false;
                }
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return true;
    }

    // can only be done 'Before Semester'
    public void dropCourse(String courseCode)
    {
        if(!getRunningPhase(1)) return;

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
