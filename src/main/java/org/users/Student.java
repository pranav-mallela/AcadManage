package org.users;

import java.sql.*;

public class Student extends User{
    public final int studentId;
    private int entry_year;

    public Student(Connection conn, int studentId) {
        super(conn);
        this.studentId = studentId;
    }

    // can only be done 'Before Semester'
    public void addCourse(String courseCode, int year, int semester) throws SQLException
    {
        if(!getRunningPhase(1)) return;

        Statement statement;
        ResultSet rs = null;

        // get the student's entry year
        String yearQuery = String.format("select * from students where student_id=%d;", this.studentId);
        statement = conn.createStatement();
        rs = statement.executeQuery(yearQuery);
        while(rs.next()) {
            this.entry_year = rs.getInt("entry_year");
        }

        // check if the course and offering exist
        int [] idArray = getCourseAndOfferingId(courseCode, year, semester);
        int courseId = idArray[0], offeringId = idArray[1];
        if(courseId == 0 || offeringId == 0) return;

        // Enforce Credit Limit
        if(!isPassingCreditLimit(year, semester, courseCode)) return;

        // get the current academic year and semester, and see if everything is ok
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
                System.out.print("SUCCESS: Course successfully enrolled!\n");
            }
            else
            {
                System.out.print("UNSUCCESSFUL ACTION: Cannot enroll due to wrong year or semester!\n");
                return;
            }
        }
    }

    // check the credit limit of the student
    private boolean isPassingCreditLimit(int year, int semester, String courseCode) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        statement = conn.createStatement();

        int year1, year2, sem1, sem2;
        if(semester == 2)
        {
            year1 = year;
            year2 = year-1;
            sem1 = 1;
            sem2 = 2;
        }
        else
        {
            year1 = year-1;
            year2 = year-1;
            sem1 = 2;
            sem2 = 1;
        }

        // get credit limits for the student in year1, sem1 and year2, sem2
        float creditLimit1, creditLimit2;

        String getCreditLimit1Query = String.format("select credit_limit from credit_limits where academic_year=%d and semester=%d;", year1, sem1);
        rs = statement.executeQuery(getCreditLimit1Query);
        if (rs.next()) creditLimit1 = rs.getFloat("credit_limit");
        else creditLimit1 = 0;

        String getCreditLimit2Query = String.format("select credit_limit from credit_limits where academic_year=%d and semester=%d;", year2, sem2);
        rs = statement.executeQuery(getCreditLimit2Query);
        if (rs.next()) creditLimit2 = rs.getFloat("credit_limit");
        else creditLimit2 = 0;

        //calculate the credit limit
        float creditLimit;
        if (creditLimit1 == 0 && creditLimit2 == 0) creditLimit = 24;
        else if(creditLimit1 == 0) creditLimit = creditLimit2*1.25F;
        else if(creditLimit2 == 0) creditLimit = creditLimit1*1.25F;
        else creditLimit = (creditLimit1 + creditLimit2)*1.25F/2;

        // get the credits of the student in year, semester
        float credits;
        String getCreditsQuery = String.format("select SUM(cat.c) as sum from student_%d s, offerings o, course_catalog cat where o.year_offered_in=%d and o.semester_offered_in=%d and cat.course_code=s.course_code group by o.year_offered_in, o.semester_offered_in;", studentId, year, semester);
        rs = statement.executeQuery(getCreditsQuery);
        if (rs.next()) credits = rs.getFloat("sum");
        else credits = 0.0F;

        // get the credits of the course
        float courseCredits = 0;
        String getCourseCreditsQuery = String.format("select c from course_catalog where course_code='%s';", courseCode);
        rs = statement.executeQuery(getCourseCreditsQuery);
        if (rs.next()) courseCredits = rs.getFloat("c");

        // check if the credits + courseCredits is less than the credit limit
        if(credits + courseCredits <= creditLimit) return true;
        else
        {
            System.out.print("UNSUCCESSFUL ACTION: Credit limit exceeded!\n");
            return false;
        }
    }

    // get the cgpa of the student and min cgpa of the offering
    // then return true if the student's cgpa is greater than or equal to the min cgpa
    private boolean isPassingCGCriteria(int offeringId) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;

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
                System.out.print("UNSUCCESSFUL ACTION: CGPA is less than the minimum required!\n");
                return false;
            }
        }
    }

    private boolean isPassingPreReqs(String courseCode) throws SQLException
    {
        Statement statement, statement1;
        ResultSet rs = null, rs1 = null;
        boolean passedPreReqs = false;

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
                    System.out.print("UNSUCCESSFUL ACTION: You have not cleared the prerequisites for this course!\n");
                    return false;
                }
            }
            passedPreReqs = true;
        }
        if(!passedPreReqs)
        {
            System.out.print("UNSUCCESSFUL ACTION: You have not cleared the prerequisites for this course!\n");
        }
        return passedPreReqs;
    }

    private boolean isPassingConstraints(int offeringId) throws SQLException
    {
        Statement statement, statement1;
        ResultSet rs = null, rs1 = null, rs2 = null;

        //if no constraints exist, mark as true
        rs = conn.createStatement().executeQuery(String.format("SELECT course_id FROM offering_constraints WHERE offering_id=%d", offeringId));
        if(!rs.next()) return true;

        // get courses set by the instructor as constraints
        String getConstraintCoursesQuery = String.format("SELECT course_id, grade from offering_constraints WHERE offering_id=%d", offeringId);
        statement = conn.createStatement();
        rs = statement.executeQuery(getConstraintCoursesQuery);
        // rs contains the courses and grades set by the instructor as constraints
        while (rs.next())
        {
            // get the grades of the student in the corresponding courses
            int courseId = rs.getInt("course_id");
            String getGradesQuery = String.format("SELECT grade FROM student_%d WHERE course_code=(SELECT course_code FROM course_catalog WHERE course_id=%d)", studentId, courseId);
            statement1 = conn.createStatement();
            rs1 = statement1.executeQuery(getGradesQuery);
            // rs1 contains the grades of the student in the corresponding constraint courses
            if (rs1.next())
            {
                // if a constraint is not met, go into the corresponding optional offering constraints
                if (rs1.getString("grade").compareTo(rs.getString("grade")) > 0)
                {
                    String checkOptionalConstraintsQuery = String.format("SELECT option_course_id, grade FROM optional_offering_constraints WHERE course_id=%d", courseId);
                    rs2 = conn.createStatement().executeQuery(checkOptionalConstraintsQuery);
                    boolean passedThisConstraint = false;
                    // rs2 contains the optional offerings and grades set by the instructor as constraints
                    while(rs2.next())
                    {
                        String getOptionalGradesQuery = String.format("SELECT grade FROM student_%d WHERE course_code=(SELECT course_code FROM course_catalog WHERE course_id=%d)", studentId, rs2.getInt("option_course_id"));
                        ResultSet rs3 = conn.createStatement().executeQuery(getOptionalGradesQuery);
                        if(rs3.next() && rs3.getString("grade").compareTo(rs2.getString("grade")) <= 0)
                        {
                            passedThisConstraint = true;
                        }
                    }
                    // if all optional offerings fail, return ERROR
                    if(!passedThisConstraint)
                    {
                        System.out.print("UNSUCCESSFUL ACTION: Constraints not passed!\n");
                        return false;
                    }
                }
            }
            else
            {
                System.out.print("UNSUCCESSFUL ACTION: Constraints not passed!\n");
                return false;
            }
        }
        return true;
    }

    // can only be done 'Before Semester'
    public void dropCourse(String courseCode) throws SQLException
    {
        if(!getRunningPhase(1)) return;

        Statement statement;
        ResultSet rs = null;

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
           System.out.print("SUCCESS: Course successfully dropped!\n");
       }
       else
       {
           System.out.print("UNSUCCESSFUL ACTION: Enrollment does not exist! Cannot drop course!\n");
       }
    }

    public void viewEnrolledCourseDetails() throws SQLException
    {
        Statement statement;
        ResultSet rs = null;

        String viewQuery = String.format("SELECT * FROM student_%d", studentId);
        statement = conn.createStatement();
        rs = statement.executeQuery(viewQuery);
        System.out.print(" offering_id | course_code | status | grade\n");
        System.out.print("-------------+-------------+--------+--------\n");
        while(rs.next())
        {
            System.out.print(" ".repeat("offering_id".length()) + rs.getInt("offering_id") + " | "
                    + rs.getString("course_code") + " ".repeat("course_code".length()-4) + "| "
                    + rs.getString("status") + " ".repeat("status".length()-1) + "| "
                    + rs.getString("grade") + "\n");
        }
    }
    public float CGPA() throws SQLException
    {
        Statement statement, statement1;
        ResultSet rs, rs1 = null;
        int cgpa = 0;
        int totalCredits = 0;

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
        if(totalCredits == 0)
        {
            return 0;
        }
        return cgpa/totalCredits;
    }
}
