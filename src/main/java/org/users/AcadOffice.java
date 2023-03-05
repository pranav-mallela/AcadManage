package org.users;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.List;

public class AcadOffice extends User{
    public AcadOffice(Connection conn) {
        super(conn);
    }

    public void addCourseToCatalog(String courseCode, float l, float t, float p, List<List<String>> optionPreReqs, String courseTitle) throws SQLException
    {
        Statement statement;

        // check if course already exists
        int courseId = checkIfCourseExists(courseCode, false);
        if(courseId != 0) return;

        //add prerequisites to pre_req table before adding to catalog
        if(optionPreReqs.size() != 0)
        {
            for (List<String> optionPreReq : optionPreReqs) {
                String preReqCode = optionPreReq.get(optionPreReq.size() - 1); // code of the compulsory prerequisite
                int preReqId = checkIfCourseExists(preReqCode, true);
                if (preReqId == 0) {
                    String deletePreReqQuery = String.format("DELETE FROM pre_req where course_code='%s'", courseCode);
                    statement = conn.createStatement();
                    statement.executeUpdate(deletePreReqQuery);
                    return;
                }
                // add it to the main prerequisite table
                String addPreReqQuery = String.format("INSERT INTO pre_req(course_code, pre_req_course_id, pre_req_code) VALUES('%s', %d, '%s')", courseCode, preReqId, preReqCode);
                statement = conn.createStatement();
                statement.executeUpdate(addPreReqQuery);
                if (!optionPreReq.get(0).equals("")) {
                    //add all of its equivalents as well, to the optional prerequisite table
                    for (int j = 0; j < optionPreReq.size() - 1; j++) {
                        // check if equivalent exists in catalog
                        int optionCourseId = checkIfCourseExists(optionPreReq.get(j), true);
                        if (optionCourseId == 0) {
                            String deleteOptionalPreReqQuery = String.format("DELETE FROM optional_pre_req WHERE pre_req_code='%s'", preReqCode);
                            statement = conn.createStatement();
                            statement.executeUpdate(deleteOptionalPreReqQuery);
                            return;
                        }

                        // add it to the optional prerequisite table
                        String addOptionalPreReqQuery = String.format("INSERT INTO optional_pre_req(pre_req_code, option_course_id, option_code) VALUES('%s', %d, '%s')", preReqCode, optionCourseId, optionPreReq.get(j));
                        statement = conn.createStatement();
                        statement.executeUpdate(addOptionalPreReqQuery);
                    }
                }
            }
        }

        //add course
        String addCourseQuery = String.format("INSERT INTO course_catalog(course_code, l, t, p, course_title) VALUES('%s', %f, %f, %f, '%s');", courseCode, l, t, p, courseTitle);
        statement = conn.createStatement();
        statement.executeUpdate(addCourseQuery);
        System.out.print("SUCCESS: Course successfully added to catalog!\n");
    }

    public void generateTranscript(int studentId, int year, int semester) throws Exception
    {
        Statement statement;
        ResultSet rs = null;

        // query to get the transcript of a student
        String getCourseIdQuery = String.format("SELECT course_id FROM course_catalog WHERE course_code=s.course_code");
        String getOfferingIdQuery = String.format("SELECT offering_id FROM offerings WHERE year_offered_in=%d and semester_offered_in=%d and course_id=(%s)", year, semester, getCourseIdQuery);
        String getStudentTranscriptQuery = String.format("SELECT course_code, grade FROM student_%d s WHERE offering_id=(%s)", studentId, getOfferingIdQuery);

        // export txt file to user's computer
        statement = conn.createStatement();
        String pathToTranscript = String.format("C:/Users/Public/Transcripts/Student_%d", studentId);
        new File(pathToTranscript).mkdirs();
        FileWriter fw = new FileWriter(String.format("%s/transcript_%d_%d.txt", pathToTranscript, year, semester));
        fw.write(String.format("Transcript of Student %d for Academic Year %d and Semester %d\n\n", studentId, year, semester));
        fw.write("course_code | grade\n");
        fw.write("------------+-------\n");
        rs = statement.executeQuery(getStudentTranscriptQuery);
        while(rs.next())
        {
            fw.write(" " + rs.getString("course_code") + " ".repeat("course_code".length()-5) + "| " + rs.getString("grade")  + "\n");
        }
        fw.close();
    }

    public void viewStudentGrades(int studentId) throws SQLException
    {
        Student student = new Student(conn, studentId);
        student.viewEnrolledCourseDetails();
    }

    public void viewOfferingGrades(int year, int semester, String courseCode) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        int facultyId = 0;

        int courseId = checkIfCourseExists(courseCode, true);
        if(courseId == 0) return;

        String getFacultyIdQuery = String.format("SELECT faculty_id FROM offerings WHERE year_offered_in=%d and semester_offered_in=%d and course_id=%d", year, semester, courseId);
        statement = conn.createStatement();
        rs = statement.executeQuery(getFacultyIdQuery);
        if(rs.next())
        {
            facultyId = rs.getInt("faculty_id");
        }
        Faculty faculty = new Faculty(conn, facultyId);
        faculty.viewGrades(year, semester, courseCode);
    }

    public boolean canGraduate(int studentId)
    {
        Statement statement;
        ResultSet rs = null;

        try{
            // get the entry year and dept of the student
            String getBatchIdQuery = String.format("SELECT entry_year, dept FROM students WHERE student_id=%d", studentId);
            statement = conn.createStatement();
            rs = statement.executeQuery(getBatchIdQuery);
            if(!rs.next())
            {
                System.out.print("UNSUCCESSFUL ACTION: Student does not exist!\n");
                return false;
            }
            int entryYear = rs.getInt("entry_year");
            String dept = rs.getString("dept");

            // check if student has completed all the core courses
            String checkCoreCompletion = String.format("" +
                    "SELECT * " +
                    "FROM pc_2020_cse " +
                    "WHERE course_code NOT IN ( " +
                    "  SELECT course_code " +
                    "  FROM student_1 " +
                    "  WHERE grade <> 'E' AND grade <> 'E-' AND grade <> 'F' " +
                    ")");
            rs = statement.executeQuery(checkCoreCompletion);
            if(rs.next())
            {
                System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the core courses!\n");
                return false;
            }

            // check if the student has completed all the elective courses
            String checkElectiveCompletion = String.format("""
                    SELECT el_%d_%s.course_category as course_category, SUM(course_catalog.c) as sum
                    FROM
                      student_%d
                      JOIN el_%d_%s ON student_%d.course_code = el_%d_%s.course_code
                      JOIN course_catalog ON student_%d.course_code = course_catalog.course_code
                    WHERE
                      student_%d.course_code IN (SELECT course_code FROM el_%d_%s)
                      AND student_%d.grade <> 'E' AND student_%d.grade <> 'E-' AND student_%d.grade <> 'F'
                    GROUP BY el_%d_%s.course_category;
                    """, entryYear, dept, studentId, entryYear, dept, studentId, entryYear, dept, studentId, studentId, entryYear, dept, studentId, studentId, studentId, entryYear, dept);
            rs = statement.executeQuery(checkElectiveCompletion);
            if(!rs.next())
            {
                System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the elective courses!\n");
                return false;
            }
            else{
                int totalElectiveCredits = 0;
                for(int i=0;i<3;i++)
                {
                    int creditsEarned = rs.getInt("sum");
                    if(creditsEarned < 6)
                    {
                        System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the elective courses!\n");
                        return false;
                    }
                    totalElectiveCredits += creditsEarned;
                    rs.next();
                }
                if(totalElectiveCredits < 24)
                {
                    System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the elective courses!\n");
                    return false;
                }
            }

            // check if the student completed all the extracurricular and capstone courses
            String checkExtracurricularCompletion = String.format("""
                   SELECT extra_cap_%d.course_category as course_category, SUM(course_catalog.c) as sum
                   FROM
                     student_%d
                     JOIN extra_cap_%d ON student_%d.course_code = extra_cap_%d.course_code
                     JOIN course_catalog ON student_%d.course_code = course_catalog.course_code
                   WHERE
                     student_%d.course_code IN (SELECT course_code FROM extra_cap_%d)
                     AND student_%d.grade <> 'E' AND student_%d.grade <> 'E-' AND student_%d.grade <> 'F'
                   GROUP BY extra_cap_%d.course_category;
                    """, entryYear, studentId, entryYear, studentId, entryYear, studentId, studentId, entryYear, studentId, studentId, studentId, entryYear);
            rs = statement.executeQuery(checkExtracurricularCompletion);
            if(!rs.next())
            {
                System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the extracurricular and capstone courses!\n");
                return false;
            }
            else{
                for(int i=0;i<3;i++)
                {
                    float creditsEarned = rs.getFloat("sum");
                    if((i == 0 && creditsEarned < 9) || (i == 1 && creditsEarned < 3.5) || (i == 2 && creditsEarned < 4))
                    {
                        System.out.print("UNSUCCESSFUL ACTION: Student has not completed all the extracurricular and capstone courses!\n");
                        return false;
                    }
                    rs.next();
                }
            }
        } catch (SQLException e)
        {
            System.out.print(e);
        }
        System.out.print("SUCCESS: Student eligible for graduation!\n");
        return true;
    }

    public void setSemesterEvent(int action) throws SQLException
    {
        Statement statement;

        String setSemesterEventQuery = "UPDATE semester_events SET is_open=0::BOOLEAN";
        statement = conn.createStatement();
        statement.executeUpdate(setSemesterEventQuery);
        String setSemesterEventQuery2 = String.format("UPDATE semester_events SET is_open=1::BOOLEAN WHERE event_id=%d", action);
        statement = conn.createStatement();
        statement.executeUpdate(setSemesterEventQuery2);
        System.out.print("SUCCESS: Semester event set\n");
    }
}
