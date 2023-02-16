package org.users;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.List;

public class AcadOffice extends User{
    public AcadOffice(Connection conn) {
        super(conn);
    }

    public void addCourseToCatalog(String courseCode, int l, int t, int p, List<List<String>> optionPreReqs)
    {
        Statement statement;

        // check if course already exists
        int courseId = checkIfCourseExists(courseCode);
        if(courseId != 0)
        {
            System.out.println("Error: Course already exists!");
            return;
        }

        //add prerequisites before adding to catalog
        if(optionPreReqs.size() != 0)
        {
            for(int i = 0;i < optionPreReqs.size();i++)
            {
                String preReqCode = optionPreReqs.get(i).get(optionPreReqs.get(i).size()-1); // code of the compulsory prerequisite
                int preReqId = checkIfCourseExists(preReqCode);
                if(preReqId == 0)
                {
                    try{
                        String deletePreReqQuery = String.format("DELETE FROM pre_req where course_code='%s'", courseCode);
                        statement = conn.createStatement();
                        statement.executeUpdate(deletePreReqQuery);
                        System.out.println("Error: Prerequisite does not exist!");
                    } catch (Exception e)
                    {
                        System.out.println(e);
                    }
                    return;
                }
                // add it to the main prerequisite table
                try{
                    String addPreReqQuery = String.format("INSERT INTO pre_req(course_code, pre_req_course_id, pre_req_code) VALUES('%s', %d, '%s')", courseCode, preReqId, preReqCode);
                    statement = conn.createStatement();
                    statement.executeUpdate(addPreReqQuery);
                } catch (Exception e){
                    System.out.println(e);
                }
                if(!optionPreReqs.get(i).get(0).equals(""))
                {
                    //add all of its equivalents as well, to the optional prerequisite table
                    for(int j=0;j < optionPreReqs.get(i).size()-1;j++)
                    {
                        // check if equivalent exists in catalog
                        int optionCourseId = checkIfCourseExists(optionPreReqs.get(i).get(j));
                        if(optionCourseId == 0)
                        {
                            try{
                                String deleteOptionalPreReqQuery = String.format("DELETE FROM optional_pre_req WHERE pre_req_code='%s'", preReqCode);
                                statement = conn.createStatement();
                                statement.executeUpdate(deleteOptionalPreReqQuery);
                            } catch (Exception e)
                            {
                                System.out.println(e);
                            }
                            System.out.println("Error: Prerequisite does not exist!");
                            return;
                        }

                        // add it to the optional prerequisite table
                        try{
                            String addOptionalPreReqQuery = String.format("INSERT INTO optional_pre_req(pre_req_code, option_course_id, option_code) VALUES('%s', %d, '%s')", preReqCode, optionCourseId, optionPreReqs.get(i).get(j));
                            statement = conn.createStatement();
                            statement.executeUpdate(addOptionalPreReqQuery);
                        } catch (Exception e)
                        {
                            System.out.println(e);
                        }
                    }
                }
            }
        }

        //add course
        try{
            String addCourseQuery = String.format("INSERT INTO course_catalog(course_code, l, t, p) VALUES('%s', %d, %d, %d);", courseCode, l, t, p);
            statement = conn.createStatement();
            statement.executeUpdate(addCourseQuery);
            System.out.println("Success: Course successfully added to catalog!");
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
/*
            String exportTranscriptQuery = String.format("COPY (%s) TO '%s/transcript_%d_%d.txt'", getStudentTranscriptQuery, pathToTranscript, year, semester);
            statement.executeUpdate(exportTranscriptQuery);
*/
        } catch (Exception e)
        {
            System.out.println(e);
        }
    }

    public void viewStudentGrades(int studentId)
    {
        Student student = new Student(conn, studentId);
        student.viewEnrolledCourseDetails();
    }

    public void viewOfferingGrades(int year, int semester, String courseCode)
    {
        Statement statement;
        ResultSet rs = null;
        int facultyId = 0;

        try{
            int courseId = checkIfCourseExists(courseCode);
            if(courseId == 0)
            {
                System.out.println("Error: Course does not exist!");
                return;
            }
            String getFacultyIdQuery = String.format("SELECT faculty_id FROM offerings WHERE year_offered_in=%d and semester_offered_in=%d and course_id=%d", year, semester, courseId);
            statement = conn.createStatement();
            rs = statement.executeQuery(getFacultyIdQuery);
            if(rs.next())
            {
                facultyId = rs.getInt("faculty_id");
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        Faculty faculty = new Faculty(conn, facultyId);
        faculty.viewGrades(year, semester, courseCode);
    }
}
