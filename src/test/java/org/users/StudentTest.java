package org.users;

import org.connection.Connect;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class StudentTest {

    static Student student;
    static ByteArrayOutputStream baos;
    static Statement statement;

    @BeforeAll
    static void setUpStudentTest() throws SQLException {
        Connect portal = new Connect("setest", "setest", "setest");
        Connection conn = portal.connect();
        student = new Student(conn, 1);
        baos = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        statement = student.conn.createStatement();
    }

    public void customAssert(String expected) {
        byte[] byteArray = baos.toByteArray();
        String output = new String(byteArray);
        Assertions.assertEquals(expected, output);
    }

    @BeforeEach
    public void resetDB() {
        baos.reset();
        try {
            String deleteUpdatesQuery = "DELETE FROM optional_offering_constraints; DELETE FROM offering_constraints; DELETE FROM optional_pre_req; DELETE FROM student_1; DELETE FROM offering_constraints; DELETE FROM offering_cg_constraints; DELETE FROM pre_req; DELETE FROM offerings; DELETE FROM course_catalog;";
            String resetSeqQuery = "SELECT setval('course_catalog_course_id_seq', 1, false); SELECT setval('offerings_offering_id_seq', 1, false);";
            statement.execute(deleteUpdatesQuery);
            statement.execute(resetSeqQuery);
        } catch (SQLException e) {
            System.out.print(e);
        }
    }

    //addCourse tests
    @Test
    public void wrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);";
        statement.execute(setWrongPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void courseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void offeringDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Offering does not exist!\n");
    }

    @Test
    public void notPassingConstraints() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);";
        String insertConstraintsQuery = "INSERT INTO offering_constraints(offering_id, course_id, grade) VALUES (1, 1, 'A');";
        String addStudentGradeQuery = "INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (1, 1, 'EN', 'B');";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(insertConstraintsQuery);
        statement.execute(addStudentGradeQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Constraints not passed!\n");
    }

    @Test
    public void notPassingPreReqs() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);";
        String addPreReqQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE101', 'Programming PreReq', 3, 1, 0); INSERT INTO pre_req(course_code, pre_req_course_id, pre_req_code) VALUES ('GE103', 2, 'GE101');";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(addPreReqQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: You have not cleared the prerequisites for this course!\n");
    }

    @Test
    public void notPassingCG() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);";
        String addStudentGradeQuery = "INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (1, 1, 'EN', 'B');";
        String addCGConstraintsQuery = "INSERT INTO offering_cg_constraints(offering_id, cg) VALUES (1, 9.0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(addStudentGradeQuery);
        statement.execute(addCGConstraintsQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: CGPA is less than the minimum required!\n");
    }

    @Test
    public void optionalPreReq() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1); INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE101', 'Programming PreReq', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 2, 2023, 1);";
        String addOptionalPreReqQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE001', 'Programming Optional PreReq', 3, 1, 0); INSERT INTO pre_req(course_code, pre_req_course_id, pre_req_code) VALUES ('GE103', 2, 'GE101'); INSERT INTO optional_pre_req(pre_req_code, option_course_id, option_code) VALUES ('GE101', 3, 'GE001');";
        String addStudentGradeQuery = "INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (2, 'GE101', 'EN', 'D-');";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(addOptionalPreReqQuery);
        statement.execute(addStudentGradeQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: You have not cleared the prerequisites for this course!\n");
        baos.reset();
        String optionalPreReqPassQuery = "INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 3, 2023, 1); INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (3, 'GE001', 'EN', 'A-');";
        statement.execute(optionalPreReqPassQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("SUCCESS: Course successfully enrolled!\n");
    }

    @Test
    public void optionalConstraints() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE103', 'Introduction to Programming', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 1, 2023, 1);";
        String insertConstraintCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE101', 'Programming PreReq', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 2, 2023, 1); INSERT INTO offering_constraints(offering_id, course_id, grade) VALUES (1, 2, 'A');";
        String insertOptionalConstraintCourseOfferingQuery = "INSERT INTO course_catalog(course_code, course_title, l, t, p) VALUES ('GE001', 'Programming Optional PreReq', 3, 1, 0); INSERT INTO offerings(faculty_id, course_id, year_offered_in, semester_offered_in) VALUES(1, 3, 2023, 1);";
        String addOptionalConstraintsQuery = "INSERT INTO optional_offering_constraints(offering_id, course_id, option_course_id, grade) VALUES (1, 2, 3, 'D');";
        String addStudentGradeQuery = "INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (2, 'GE101', 'EN', 'D-'); INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (3, 'GE001', 'EN', 'D-');";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(insertConstraintCourseOfferingQuery);
        statement.execute(insertOptionalConstraintCourseOfferingQuery);
        statement.execute(addOptionalConstraintsQuery);
        statement.execute(addStudentGradeQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Constraints not passed!\n");
        baos.reset();
        String optionalConstraintsPassQuery = "UPDATE student_1 SET grade = 'A' WHERE offering_id = 3";
        statement.execute(optionalConstraintsPassQuery);
        student.addCourse("GE103", 2023, 1);
        customAssert("SUCCESS: Course successfully enrolled!\n");
    }
}