package org.users;

import org.connection.Connect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Scanner;

class FacultyTest {

    static Faculty faculty;
    static ByteArrayOutputStream baos;
    static Statement statement;

    @BeforeAll
    static void setUpFacultyTest() throws SQLException {
        Connect portal = new Connect("setest", "setest", "setest");
        Connection conn = portal.connect();
        faculty = new Faculty(conn, 1);
        baos = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        statement = faculty.conn.createStatement();
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
            String deleteUpdatesQuery = "DELETE FROM student_1; DELETE FROM offering_cg_constraints; DELETE FROM optional_offering_constraints; DELETE FROM offering_constraints; DELETE FROM pre_req; DELETE FROM offerings; DELETE FROM course_catalog;";
            String resetSeqQuery = "SELECT setval('course_catalog_course_id_seq', 1, false); SELECT setval('offerings_offering_id_seq', 1, false);";
            statement.execute(deleteUpdatesQuery);
            statement.execute(resetSeqQuery);
        } catch (SQLException e) {
            System.out.print(e);
        }
    }

    //floatCourse tests
    @Test
    public void wrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        statement.execute(setWrongPhaseQuery);
        faculty.floatCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void courseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        faculty.floatCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void offeringExists() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        faculty.floatCourse("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Offering already exists!\n");
    }

    @Test
    public void wrongSemester() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.floatCourse("GE103", 2023, 2);
        customAssert("UNSUCCESSFUL ACTION: Cannot float due to wrong year or semester!\n");
    }

    @Test
    public void floatSuccess() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.floatCourse("GE103", 2023, 1);
        customAssert("SUCCESS: Course successfully floated!\n");
    }

    //addConstraint tests
    @Test
    public void addConstraintWrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        statement.execute(setWrongPhaseQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE101", "A"))));
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void constraintCourseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE101", "A"))));
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void constraintOfferingDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE101", "A"))));
        customAssert("UNSUCCESSFUL ACTION: Offering does not exist!\n");
    }

    @Test
    public void mainConstraintDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE101", "A"))));
        customAssert("UNSUCCESSFUL ACTION: Course GE101 does not exist!\n");
    }

    @Test
    public void addConstraintSuccess() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String insertMainConstraintQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE101', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(insertMainConstraintQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE101", "A"))));
        customAssert("SUCCESS: Constraints successfully added!\n");
    }

    @Test
    public void optionalConstraint() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String insertMainConstraintQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE101', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(insertMainConstraintQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE102", "A"), List.of("GE101", "A"))));
        customAssert("UNSUCCESSFUL ACTION: Course GE102 does not exist!\n");
        baos.reset();
        String deleteConstraintsQuery = "DELETE FROM offering_constraints WHERE offering_id = 1";
        String insertOptionalConstraintQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE102', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(deleteConstraintsQuery);
        statement.execute(insertOptionalConstraintQuery);
        faculty.addConstraintsToOffering(2023, 1, "GE103", List.of(List.of(List.of("GE102", "A"), List.of("GE101", "A"))));
        customAssert("SUCCESS: Constraints successfully added!\n");
    }

    //addCGConstraints tests
    @Test
    public void addCGConstraintWrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        statement.execute(setWrongPhaseQuery);
        faculty.addCGConstraints(2023, 1, "GE103", 9.00F);
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void addCGConstraintCourseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        faculty.addCGConstraints(2023, 1, "GE103", 9.00F);
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void addCGConstraintOfferingDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.addCGConstraints(2023, 1, "GE103", 9.00F);
        customAssert("UNSUCCESSFUL ACTION: Offering does not exist!\n");
    }

    @Test
    public void CGConstraintsSuccess() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        faculty.addCGConstraints(2023, 1, "GE103", 9.00F);
        customAssert("SUCCESS: CG Constraints successfully added!\n");
    }

    //cancelOffering tests
    @Test
    public void cancelOfferingWrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        statement.execute(setWrongPhaseQuery);
        faculty.cancelOffering("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void cancelOfferingCourseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        faculty.cancelOffering("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void cancelOfferingOfferingDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.cancelOffering("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Offering does not exist!\n");
    }

    @Test
    public void cancelOfferingWrongSemester() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String updateUpcomingSemToPast = "UPDATE upcoming_semester SET academic_year = 2022, semester = 2";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2022, 2);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(updateUpcomingSemToPast);
        statement.execute(insertCourseOfferingQuery);
        String updateUpcomingSemToPresent = "UPDATE upcoming_semester SET academic_year = 2023, semester = 1";
        statement.execute(updateUpcomingSemToPresent);
        faculty.cancelOffering("GE103", 2022, 2);
        customAssert("UNSUCCESSFUL ACTION: Cannot cancel offering that is not in upcoming semester!\n");
    }

    @Test
    public void cancelOfferingSuccess() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String enrollStudentQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES (1, 'GE103', 'EN'); INSERT INTO offering_1 VALUES(1)";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(enrollStudentQuery);
        faculty.cancelOffering("GE103", 2023, 1);
        customAssert("SUCCESS: Offering cancelled successfully!\n");
    }

    //uploadGrades tests
    @Test
    public void uploadGradesWrongPhase() throws SQLException {
        String setWrongPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 1 THEN true ELSE false END";
        statement.execute(setWrongPhaseQuery);
        faculty.uploadGrades("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Cannot perform action in this phase!\n");
    }

    @Test
    public void uploadGradesCourseDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        statement.execute(setCorrectPhaseQuery);
        faculty.uploadGrades("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Course GE103 does not exist!\n");
    }

    @Test
    public void uploadGradesOfferingDoesNotExist() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseQuery);
        faculty.uploadGrades("GE103", 2023, 1);
        customAssert("UNSUCCESSFUL ACTION: Offering does not exist!\n");
    }

    @Test
    public void uploadGradesSuccess() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String enrollStudentQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES (1, 'GE103', 'EN'); INSERT INTO offering_1 VALUES(1)";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(enrollStudentQuery);
        faculty.uploadGrades("GE103", 2023, 1);
        customAssert("\nCheck the directory C:/Users/Public/Grades_<courseID>/offering_<offeringID>.csv for the csv file containing enrolled students' information.\nPress enter once all the grades have been updated and the file has been saved: SUCCESS: Grades have been successfully updated!\n");
    }

    //viewGrades tests
    @Test
    public void viewGrades() throws SQLException {
        String setCorrectPhaseQuery = "UPDATE semester_events SET is_open = CASE WHEN event_id = 3 THEN true ELSE false END";
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String enrollStudentQuery = "INSERT INTO student_1(offering_id, course_code, status, grade) VALUES (1, 'GE103', 'EN', 'A'); INSERT INTO offering_1 VALUES(1, 'A')";
        statement.execute(setCorrectPhaseQuery);
        statement.execute(insertCourseOfferingQuery);
        statement.execute(enrollStudentQuery);
        faculty.viewGrades(2023, 1, "GE103");
    }


}