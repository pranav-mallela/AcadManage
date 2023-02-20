package org.users;

import org.connection.Connect;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    Connect portal = new Connect();
    Connection conn = portal.connect();
    User user = new User(conn);
    int id = user.login("pstudent1", "1234", "0");
    Student student = new Student(conn, id);

    // course does not exist
    // offering does not exist
    // acad year does not match with upcoming acad year
    // semester does not match with upcoming semester
    // matches, but the student has completed four years
    // not passing faculty constraints
    // not passing pre-reqs
    // not passing CG criteria
    @Test
    void courseDoesNotExist() {
        assertTrue(student.addCourse("CS100", 2023, 1) < 0);
    }
    @Test
    void offeringDoesNotExist() {
        assertTrue(student.addCourse("CS101", 2023, 1) < 0);
    }

    @Test
    void acadYearDoesNotMatch() {
        assertTrue(student.addCourse("GE103", 2022, 1) < 0);
    }

    @Test
    void notPassingCGCriteria() {
        assertTrue(student.addCourse("GE103", 2023, 1) < 0);
    }

    @Test
    void notPassingPreReqs() {
        assertTrue(student.addCourse("CS201", 2023, 1) < 0);
    }

    @Test
    void notPassingConstraints() {
        assertTrue(student.addCourse("MA201", 2023, 1) < 0);
    }

    @Test
    void dropCourse() {

    }

    @Test
    void viewEnrolledCourseDetails() {
    }

    @Test
    void CGPA() {
    }
}