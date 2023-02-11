package org.users;

import java.sql.Connection;

public class Faculty extends User{
    private int facultyId;
    private String dept;

    public Faculty(Connection conn) {
        super(conn);
    }

    public void floatCourse(String courseCode, int year, int semester)
    {
        // float course
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
