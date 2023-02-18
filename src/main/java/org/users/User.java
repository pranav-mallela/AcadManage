package org.users;

import java.sql.*;

public class User {
    private String name;
    private String username;
    private String phone;
    private String password;
    protected Connection conn;

    public User(Connection conn)
    {
        this.conn = conn;
    }
    public int login(String username, String password, String role)
    {
        Statement statement;
        ResultSet rs = null;
        try{
            String query="";
            if(role.equals("0"))
            {
                query = String.format("select * from student_credentials where username='%s' and pass='%s'", username, password);
            }
            else if(role.equals("1"))
            {
                query = String.format("select * from faculty_credentials where username='%s' and pass='%s'", username, password);
            }
            else if(role.equals("2"))
            {
                query = String.format("select * from acad_credentials where username='%s' and pass='%s'", username, password);
            }
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            while(rs.next())
            {
                return rs.getInt("id");
            }
        }catch(Exception e)
        {
            System.out.println(e);
        }
        return -1;
    }

    protected int checkIfCourseExists(String courseCode, boolean wantToExist)
    {
        Statement statement;
        ResultSet rs = null;
        int courseId=0;
        try{
            String courseQuery = String.format("select * from course_catalog where course_code='%s';", courseCode);
            statement = conn.createStatement();
            rs = statement.executeQuery(courseQuery);
            if(rs.next())
            {
                courseId = rs.getInt("course_id");
            }
        } catch(Exception e) {
            System.out.println(e);
        }
        if(wantToExist && courseId == 0)
        {
            System.out.printf("ERROR: Course %s does not exist!", courseCode);
        }
        else if(!wantToExist && courseId != 0)
        {
            System.out.printf("ERROR: Course %s already exists!", courseCode);
        }
        return courseId;
    }

    protected int checkIfOfferingExists(String courseCode, int courseId, int year, int semester)
    {
        Statement statement;
        ResultSet rs = null;
        int offeringId=0;
        try{
            String courseQuery = String.format("select * from offerings where course_id='%s' and year_offered_in=%d and semester_offered_in=%d;", courseId, year, semester);
            statement = conn.createStatement();
            rs = statement.executeQuery(courseQuery);
            if(rs.next())
            {
                offeringId = rs.getInt("offering_id");
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return offeringId;
    }

    protected boolean checkIfUpcomingSem(int year, int semester)
    {
        Statement statement;
        ResultSet rs = null;
        int acad_year=0, acad_sem=0;
        try{
            String semQuery = "select * from upcoming_semester where upcoming=(1::boolean);";
            statement = conn.createStatement();
            rs = statement.executeQuery(semQuery);
            if(rs.next())
            {
                acad_year = rs.getInt("academic_year");
                acad_sem = rs.getInt("semester");
            }
        } catch (Exception e)
        {
            System.out.println(e);
        }
        return (acad_year == year && acad_sem == semester);
    }

    protected int [] getCourseAndOfferingId(String courseCode, int year, int semester)
    {
        int courseId = checkIfCourseExists(courseCode, true);
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(courseId != 0 && offeringId == 0)
        {
            System.out.println("ERROR: Offering does not exist!");
        }
        return new int[] {courseId, offeringId};
    }


    public void resetPass(String email)
    {
        //reset password
    }

    public void editProfile()
    {
        // edit profile
    }
}
