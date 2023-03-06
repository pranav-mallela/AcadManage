package org.users;

import org.connection.Connect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

class AcadOfficeTest {
    static AcadOffice acadOffice;
    static ByteArrayOutputStream baos;
    static Statement statement;

    @BeforeAll
    static void setUpFacultyTest() throws SQLException {
        Connect portal = new Connect("setest", "setest", "setest");
        Connection conn = portal.connect();
        acadOffice = new AcadOffice(conn);
        baos = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        statement = acadOffice.conn.createStatement();
    }

    public void customAssert(String expected) {
        String output = baos.toString();
        Assertions.assertEquals(expected, output);
    }

    @BeforeEach
    public void resetDB() {
        baos.reset();
        try {
            String deleteUpdatesQuery = "DELETE FROM upcoming_semester; INSERT INTO upcoming_semester(academic_year, semester, upcoming) VALUES(2023, 1, (1::boolean)); DELETE FROM extra_cap_2020; DELETE FROM pc_2020_cse; DELETE FROM el_2020_cse; DELETE FROM student_1; DELETE FROM optional_pre_req; DELETE FROM pre_req; DELETE FROM offerings; DELETE FROM course_catalog;";
            String resetSeqQuery = "SELECT setval('course_catalog_course_id_seq', 1, false); SELECT setval('offerings_offering_id_seq', 1, false);";
            statement.execute(deleteUpdatesQuery);
            statement.execute(resetSeqQuery);
        } catch (SQLException e) {
            System.out.print(e);
        }
    }

    //addCourseToCatalog tests
    @Test
    public void courseAlreadyExists() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(insertCourseQuery);
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(), "Intro to Engineering");
        customAssert("UNSUCCESSFUL ACTION: Course GE103 already exists!\n");
    }

    @Test
    public void noPreReq() throws SQLException {
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(), "Intro to Engineering");
        customAssert("SUCCESS: Course successfully added to catalog!\n");
    }

    @Test
    public void mainPreReqDoesNotExist() throws SQLException {
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(List.of("GE101")), "Intro to Engineering");
        customAssert("UNSUCCESSFUL ACTION: Course GE101 does not exist!\n");
    }

    @Test
    public void noOptionalPreReq() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE101', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(insertCourseQuery);
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(List.of("GE101")), "Intro to Engineering");
        customAssert("SUCCESS: Course successfully added to catalog!\n");
    }

    @Test
    public void optionalPreReqDoesNotExist() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE101', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(insertCourseQuery);
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(List.of("GE102", "GE101")), "Intro to Engineering");
        customAssert("UNSUCCESSFUL ACTION: Course GE102 does not exist!\n");
    }

    @Test
    public void addCourseSuccess() throws SQLException {
        String insertCoursesQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE101', 'Intro to Engineering', 3, 1, 0), ('GE102', 'Intro to Engineering', 3, 1, 0);";
        statement.execute(insertCoursesQuery);
        acadOffice.addCourseToCatalog("GE103", 3, 1, 0, List.of(List.of("GE102", "GE101")), "Intro to Engineering");
        customAssert("SUCCESS: Course successfully added to catalog!\n");
    }

    //generateTranscript tests
    @Test
    public void passiveFunctionTesting() throws Exception{
        String insertCourseOfferingQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1);";
        String enrollStudentQuery = "INSERT INTO student_1(offering_id, course_code, status) VALUES (1, 'GE103', 'EN'); INSERT INTO offering_1 VALUES(1)";
        statement.execute(insertCourseOfferingQuery);
        statement.execute(enrollStudentQuery);
        acadOffice.generateTranscript(1, 2023, 1);
        acadOffice.viewStudentGrades(1);
        acadOffice.viewOfferingGrades(2023, 1, "GE103");
    }

    @Test
    public void setSemesterEvent1() throws SQLException {
        acadOffice.setSemesterEvent(1);
        customAssert("SUCCESS: Semester event set\n");
    }

    @Test
    public void setSemesterEvent2() throws SQLException {
        acadOffice.setSemesterEvent(2);
        customAssert("SUCCESS: Semester event set\n");
    }

    @Test
    public void setSemesterEvent3() throws SQLException {
        acadOffice.setSemesterEvent(3);
        customAssert("SUCCESS: Semester event set\n");
    }

    //graduation tests
    @Test
    public void studentDoesNotExist() throws SQLException {
        acadOffice.canGraduate(2);
        customAssert("UNSUCCESSFUL ACTION: Student does not exist!\n");
    }

    @Test
    public void coreNotComplete() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('GE103', 'Intro to Engineering', 3, 1, 0);";
        String insertCoreCourseQuery = "INSERT INTO pc_2020_cse (course_id, course_code, course_category) VALUES (1, 'GE103', 'PC');";
        statement.execute(insertCourseQuery);
        statement.execute(insertCoreCourseQuery);
        acadOffice.canGraduate(1);
        customAssert("UNSUCCESSFUL ACTION: Student has not completed all the core courses!\n");
    }

    @Test
    public void electiveNotComplete() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('OE103', 'Intro to Engineering', 6, 1, 0), ('SE104', 'Intro to Engineering', 6, 1, 0), ('HE105', 'Intro to Engineering', 6, 1, 0), ('PE106', 'Intro to Engineering', 6, 1, 0), ('GE103', 'Intro to Engineering', 3, 1, 0);";
        String insertCoreCourseQuery = "INSERT INTO pc_2020_cse (course_id, course_code, course_category) VALUES (5, 'GE103', 'PC');";
        String insertCoreOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 5, 2023, 1);";
        String studentPassCoreCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (1, 'GE103', 'EN', 'A'); INSERT INTO offering_1 VALUES(1, 'A');";
        String insertElectiveCourseQuery = "INSERT INTO el_2020_cse (course_id, course_code, course_category) VALUES (1, 'OE103', 'PE'), (2, 'SE104', 'SE'), (3, 'HE105', 'HE'), (4, 'PE106', 'PE');";
        statement.execute(insertCourseQuery);
        statement.execute(insertCoreCourseQuery);
        statement.execute(insertCoreOfferingQuery);
        statement.execute(studentPassCoreCourseQuery);
        statement.execute(insertElectiveCourseQuery);
        acadOffice.canGraduate(1);
        customAssert("UNSUCCESSFUL ACTION: Student has not completed all the elective courses!\n");
    }

    @Test
    public void extraCapNotComplete() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('OE103', 'Intro to Engineering', 6, 1, 0), ('SE104', 'Intro to Engineering', 6, 1, 0), ('HE105', 'Intro to Engineering', 6, 1, 0), ('PE106', 'Intro to Engineering', 6, 1, 0), ('GE103', 'Intro to Engineering', 3, 1, 0);";
        String insertElectiveCourseQuery = "INSERT INTO el_2020_cse (course_id, course_code, course_category) VALUES (1, 'OE103', 'PE'), (2, 'SE104', 'SE'), (3, 'HE105', 'HE'), (4, 'PE106', 'PE');";
        String insertElectiveOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1), (1, 2, 2023, 1), (1, 3, 2023, 1), (1, 4, 2023, 1);";
        String studentPassElectiveCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (1, 'OE103', 'EN', 'A'), (2, 'SE104', 'EN', 'A'), (3, 'HE105', 'EN', 'A'), (4, 'PE106', 'EN', 'A'); INSERT INTO offering_1 VALUES(1, 'A'); INSERT INTO offering_2 VALUES(1, 'A'); INSERT INTO offering_3 VALUES(1, 'A'); INSERT INTO offering_4 VALUES(1, 'A');";
        String insertCoreCourseQuery = "INSERT INTO pc_2020_cse (course_id, course_code, course_category) VALUES (5, 'GE103', 'PC');";
        String insertCoreOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 5, 2023, 1);";
        String studentPassCoreCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (5, 'GE103', 'EN', 'A'); INSERT INTO offering_5 VALUES(1, 'A');";
        String insertExtraCapCourseToCatalogQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('II103', 'Intro to Engineering', 3.5, 1, 0), ('NN104', 'Intro to Engineering', 4, 1, 0), ('CP105', 'Intro to Engineering', 9, 1, 0)";
        String insertExtraCapCoursesQuery = "INSERT INTO extra_cap_2020 (course_id, course_code, course_category) VALUES (6, 'II103', 'II'), (7, 'NN104', 'NN'), (8, 'CP105', 'CP');";
        statement.execute(insertCourseQuery);
        statement.execute(insertElectiveCourseQuery);
        statement.execute(insertElectiveOfferingQuery);
        statement.execute(studentPassElectiveCourseQuery);
        statement.execute(insertCoreCourseQuery);
        statement.execute(insertCoreOfferingQuery);
        statement.execute(studentPassCoreCourseQuery);
        statement.execute(insertExtraCapCourseToCatalogQuery);
        statement.execute(insertExtraCapCoursesQuery);
        acadOffice.canGraduate(1);
        customAssert("UNSUCCESSFUL ACTION: Student has not completed all the extracurricular and capstone courses!\n");
    }

    @Test
    public void gradSuccess() throws SQLException {
        String insertCourseQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('OE103', 'Intro to Engineering', 6, 1, 0), ('SE104', 'Intro to Engineering', 6, 1, 0), ('HE105', 'Intro to Engineering', 6, 1, 0), ('PE106', 'Intro to Engineering', 6, 1, 0), ('GE103', 'Intro to Engineering', 3, 1, 0);";
        String insertElectiveCourseQuery = "INSERT INTO el_2020_cse (course_id, course_code, course_category) VALUES (1, 'OE103', 'PE'), (2, 'SE104', 'SE'), (3, 'HE105', 'HE'), (4, 'PE106', 'PE');";
        String insertElectiveOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 1, 2023, 1), (1, 2, 2023, 1), (1, 3, 2023, 1), (1, 4, 2023, 1);";
        String studentPassElectiveCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (1, 'OE103', 'EN', 'A'), (2, 'SE104', 'EN', 'A'), (3, 'HE105', 'EN', 'A'), (4, 'PE106', 'EN', 'A'); INSERT INTO offering_1 VALUES(1, 'A'); INSERT INTO offering_2 VALUES(1, 'A'); INSERT INTO offering_3 VALUES(1, 'A'); INSERT INTO offering_4 VALUES(1, 'A');";
        String insertCoreCourseQuery = "INSERT INTO pc_2020_cse (course_id, course_code, course_category) VALUES (5, 'GE103', 'PC');";
        String insertCoreOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 5, 2023, 1);";
        String studentPassCoreCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (5, 'GE103', 'EN', 'A'); INSERT INTO offering_5 VALUES(1, 'A');";
        String insertExtraCapCourseToCatalogQuery = "INSERT INTO course_catalog (course_code, course_title, l, t, p) VALUES ('II103', 'Intro to Engineering', 3.5, 1, 0), ('NN104', 'Intro to Engineering', 4, 1, 0), ('CP105', 'Intro to Engineering', 9, 1, 0)";
        String insertExtraCapCoursesQuery = "INSERT INTO extra_cap_2020 (course_id, course_code, course_category) VALUES (6, 'II103', 'II'), (7, 'NN104', 'NN'), (8, 'CP105', 'CP');";
        String insertExtraCapOfferingQuery = "INSERT INTO offerings (faculty_id, course_id, year_offered_in, semester_offered_in) VALUES (1, 6, 2023, 1), (1, 7, 2023, 1), (1, 8, 2023, 1);";
        String studentPassExtraCapCourseQuery = "INSERT INTO student_1 (offering_id, course_code, status, grade) VALUES (6, 'II103', 'EN', 'A'), (7, 'NN104', 'EN', 'A'), (8, 'CP105', 'EN', 'A'); INSERT INTO offering_6 VALUES(1, 'A'); INSERT INTO offering_7 VALUES(1, 'A'); INSERT INTO offering_8 VALUES(1, 'A');";
        statement.execute(insertCourseQuery);
        statement.execute(insertElectiveCourseQuery);
        statement.execute(insertElectiveOfferingQuery);
        statement.execute(studentPassElectiveCourseQuery);
        statement.execute(insertCoreCourseQuery);
        statement.execute(insertCoreOfferingQuery);
        statement.execute(studentPassCoreCourseQuery);
        statement.execute(insertExtraCapCourseToCatalogQuery);
        statement.execute(insertExtraCapCoursesQuery);
        statement.execute(insertExtraCapOfferingQuery);
        statement.execute(studentPassExtraCapCourseQuery);
        acadOffice.canGraduate(1);
        customAssert("SUCCESS: Student eligible for graduation!\n");
    }


}