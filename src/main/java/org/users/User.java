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
            String query = switch (role) {
                case "0" ->
                        String.format("select * from student_credentials where username='%s' and pass='%s'", username, password);
                case "1" ->
                        String.format("select * from faculty_credentials where username='%s' and pass='%s'", username, password);
                case "2" ->
                        String.format("select * from acad_credentials where username='%s' and pass='%s'", username, password);
                default -> "";
            };
            statement = conn.createStatement();
            rs = statement.executeQuery(query);
            if(rs.next())
            {
                return rs.getInt("id");
            }
        }catch(SQLException e)
        {
            System.out.print(e);
        }
        return -1;
    }

    protected int checkIfCourseExists(String courseCode, boolean wantToExist) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        int courseId=0;
        String courseQuery = String.format("select * from course_catalog where course_code='%s';", courseCode);
        statement = conn.createStatement();
        rs = statement.executeQuery(courseQuery);
        if(rs.next())
        {
            courseId = rs.getInt("course_id");
        }
        if(wantToExist && courseId == 0)
        {
            System.out.printf("UNSUCCESSFUL ACTION: Course %s does not exist!\n", courseCode);
        }
        else if(!wantToExist && courseId != 0)
        {
            System.out.printf("UNSUCCESSFUL ACTION: Course %s already exists!\n", courseCode);
        }
        return courseId;
    }

    protected int checkIfOfferingExists(String courseCode, int courseId, int year, int semester) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        int offeringId=0;
        String courseQuery = String.format("select * from offerings where course_id='%s' and year_offered_in=%d and semester_offered_in=%d;", courseId, year, semester);
        statement = conn.createStatement();
        rs = statement.executeQuery(courseQuery);
        if(rs.next())
        {
            offeringId = rs.getInt("offering_id");
        }
        return offeringId;
    }

    protected boolean checkIfUpcomingSem(int year, int semester) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        int acad_year=0, acad_sem=0;
        String semQuery = "select * from upcoming_semester where upcoming=(1::boolean);";
        statement = conn.createStatement();
        rs = statement.executeQuery(semQuery);
        if(rs.next())
        {
            acad_year = rs.getInt("academic_year");
            acad_sem = rs.getInt("semester");
        }
        return (acad_year == year && acad_sem == semester);
    }

    protected int [] getCourseAndOfferingId(String courseCode, int year, int semester) throws SQLException
    {
        int courseId = checkIfCourseExists(courseCode, true);
        int offeringId = checkIfOfferingExists(courseCode, courseId, year, semester);
        if(courseId != 0 && offeringId == 0)
        {
            System.out.print("UNSUCCESSFUL ACTION: Offering does not exist!\n");
        }
        return new int[] {courseId, offeringId};
    }

    public boolean getRunningPhase(int expectedPhase) throws SQLException
    {
        Statement statement;
        ResultSet rs = null;
        int phase = 0;

        String getPhaseQuery = "select event_id, event_description from semester_events where is_open=(1::boolean)";
        statement = conn.createStatement();
        rs = statement.executeQuery(getPhaseQuery);
        if(rs.next())
        {
            phase = rs.getInt("event_id");
        }
        if(phase!=expectedPhase)
        {
            System.out.print("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
            return false;
        }
        else return true;
    }

    public void updatePhone(int id, int role, String phone) throws SQLException
    {
        Statement statement;
        String updateStudentPhoneQuery = String.format("update students set phone='%s' where student_id=%d;", phone, id);
        String updateFacultyPhoneQuery = String.format("update faculty set phone='%s' where faculty_id=%d;", phone, id);
        statement = conn.createStatement();
        if(role == 0) statement.executeUpdate(updateStudentPhoneQuery);
        else if(role == 1) statement.executeUpdate(updateFacultyPhoneQuery);
        else System.out.print("UNSUCCESSFUL ACTION: Invalid role!\n");
    }

    public void updateAddress(int id, int role, String address) throws SQLException
    {
        Statement statement;
        String updateStudentAddressQuery = String.format("update students set address='%s' where student_id=%d;", address, id);
        String updateFacultyAddressQuery = String.format("update faculty set address='%s' where faculty_id=%d;", address, id);
        statement = conn.createStatement();
        if(role == 0) statement.executeUpdate(updateStudentAddressQuery);
        else if(role == 1) statement.executeUpdate(updateFacultyAddressQuery);
        else System.out.print("UNSUCCESSFUL ACTION: Invalid role!\n");
    }
}
