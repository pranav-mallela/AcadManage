package org.connection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.users.AcadOffice;
import org.users.Faculty;
import org.users.Student;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UITest {
    static UI ui;
    static Statement statement;
    static Student student;
    static Faculty faculty;
    static AcadOffice acadOffice;
    static Connection conn;

    @BeforeAll
    static void UISetUp() throws SQLException {
        Connect portal = new Connect("setest", "setest", "setest");
        conn = portal.connect();
        ui = new UI();
        statement = conn.createStatement();
        studentSetUp(conn);
        facultySetUp(conn);
        acadOfficeSetUp(conn);
    }

    static void studentSetUp(Connection conn) throws SQLException {
        student = new Student(conn, 1);
    }

    static void facultySetUp(Connection conn) throws SQLException {
        faculty = new Faculty(conn, 1);
    }

    static void acadOfficeSetUp(Connection conn) throws SQLException {
        acadOffice = new AcadOffice(conn);
    }

    @BeforeEach
    void resetDB()
    {
        try {
            String deleteUpdatesQuery = "DELETE FROM student_1; DELETE FROM pre_req; DELETE FROM offerings; DELETE FROM course_catalog;";
            String resetSeqQuery = "SELECT setval('course_catalog_course_id_seq', 1, false); SELECT setval('offerings_offering_id_seq', 1, false);";
            statement.execute(deleteUpdatesQuery);
            statement.execute(resetSeqQuery);
        } catch (SQLException e) {
            System.out.print(e);
        }
    }

    public void simulateInput(String input) {
        InputStream in = new ByteArrayInputStream((input+"\n").getBytes());
        System.setIn(in);
    }

    @Test
    void getCredentials() {
        simulateInput("0\npstudent1\n1234");
        ArrayList<String> credentials = ui.getCredentials();
        assertEquals("pstudent1", credentials.get(0));
        assertEquals("1234", credentials.get(1));
        assertEquals("0", credentials.get(2));
    }

    //studentMenu
    @Test
    void studentMenuLogout() {
        simulateInput("0");
    }

    @Test
    void studentMenuAddCourse() throws Exception {
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        statement.executeUpdate(insertCourseOfferingQuery);
        simulateInput("1\n2023\n1\nGE103\n0");
        ui.studentMenu(student);
    }

    @Test
    void studentMenuAddCourseWrongInt() throws Exception {
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        statement.executeUpdate(insertCourseOfferingQuery);
        simulateInput("wrongInput\n1\n2023\n1\nGE103\n0");
        ui.studentMenu(student);
    }

    @Test
    void studentMenuDropCourse() throws Exception {
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        String insertStudentCourseQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES(1, 'GE103', 'EN'); INSERT INTO offering_1(student_id) VALUES(1)";
        statement.executeUpdate(insertCourseOfferingQuery);
        statement.executeUpdate(insertStudentCourseQuery);
        simulateInput("2\nGE103\n0");
        ui.studentMenu(student);
    }

    @Test
    void studentMenuDropCourseWrongCode() throws Exception {
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        String insertStudentCourseQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES(1, 'GE103', 'EN'); INSERT INTO offering_1(student_id) VALUES(1)";
        statement.executeUpdate(insertCourseOfferingQuery);
        statement.executeUpdate(insertStudentCourseQuery);
        simulateInput("2\nwrongCourseCode\nGE103\n0");
        ui.studentMenu(student);
    }

    @Test
    void studentMenuViewCourses() throws Exception {
        simulateInput("3\n0");
        ui.studentMenu(student);
    }

    @Test
    void studentMenuCGPA() throws Exception {
        simulateInput("4\n0");
        ui.studentMenu(student);
    }

    //facultyMenu
    @Test
    void facultyMenuLogout() {
        simulateInput("0");
    }

    @Test
    void facultyMenuFloatCourse() throws Exception {
        String insertCourseQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0)";
        statement.executeUpdate(insertCourseQuery);
        simulateInput("1\n2023\n1\nGE103\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuCancelOffering() throws Exception {
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        statement.executeUpdate(insertCourseOfferingQuery);
        simulateInput("2\n2023\n1\nGE103\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuUploadGrades() throws Exception {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES('GE103', 'Engineering Mathematics', 3, 1, 0); INSERT INTO offerings(course_id, faculty_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1)";
        String insertStudentCourseQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES(1, 'GE103', 'EN'); INSERT INTO offering_1(student_id) VALUES(1)";
        statement.executeUpdate(setCorrectPhaseQuery);
        statement.executeUpdate(insertCourseOfferingQuery);
        statement.executeUpdate(insertStudentCourseQuery);
        simulateInput("3\n2023\n1\nGE103\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuViewGrades() throws Exception {
        simulateInput("4\n2023\n1\nGE103\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuAddConstraints() throws Exception {
        simulateInput("5\n2023\n1\nGE103\nGE101\nGE100\nA\n\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuAddConstraintsWrongGrade() throws Exception {
        simulateInput("5\n2023\n1\nGE103\nGE101\nGE100\nwrongGrade\nA\n\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuCGConstraints() throws Exception {
        simulateInput("6\n2023\n1\nGE103\n8.00\n0");
        ui.facultyMenu(faculty);
    }

    @Test
    void facultyMenuCGConstraintsInvalidFloat() throws Exception {
        simulateInput("6\n2023\n1\nGE103\ninvalidFloat\n8.00\n0");
        ui.facultyMenu(faculty);
    }

    //acadMenu tests
    @Test
    void acadMenuLogout() throws Exception {
        simulateInput("0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void acadMenuAddCourse() throws Exception {
        simulateInput("1\n3\n1\n0\nGE103\nIntro To Programming\nGE101\nGE100\n0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void generateTranscript() throws Exception {
        simulateInput("2\n1\n2023\n1\n0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void acadMenuViewStudentGrades() throws Exception {
        simulateInput("3\n1\n0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void acadMenuViewOfferingGrades() throws Exception {
        simulateInput("4\n2023\n1\nGE103\n0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void acadMenuGradCheck() throws Exception {
        simulateInput("5\n1\n0");
        ui.acadMenu(acadOffice);
    }

    @Test
    void acadMenuSetEvent() throws Exception {
        simulateInput("6\n1\n0");
        ui.acadMenu(acadOffice);
    }

}