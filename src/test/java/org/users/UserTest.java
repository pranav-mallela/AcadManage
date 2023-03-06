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

class UserTest {

    static Statement statement;
    static ByteArrayOutputStream baos;

    static Student student;
    static Faculty faculty;
    static AcadOffice acadOffice;
    static Connection conn;
    @BeforeAll
    static void userSetUp() throws SQLException {
        Connect portal = new Connect("setest", "setest", "setest");
        conn = portal.connect();
        baos = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        statement = conn.createStatement();
        studentSetUp(conn);
        facultySetUp(conn);
        acadOfficeSetUp(conn);
    }

    @BeforeEach
    public void reset() {
        baos.reset();
    }

    public void customAssert(String expected) {
        String output = baos.toString();
        Assertions.assertEquals(expected, output);
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

    @Test
    void updatePhoneStudent() throws SQLException {
        student.updatePhone(1, 0, "1111111111");
        customAssert("SUCCESS: Phone updated!\n");
    }

    @Test
    void updateAddressStudent() throws SQLException {
        student.updateAddress(1, 0, "Ropar");
        customAssert("SUCCESS: Address updated!\n");
    }

    @Test
    void updatePhoneFaculty() throws SQLException {
        faculty.updatePhone(1, 1, "1111111111");
        customAssert("SUCCESS: Phone updated!\n");
    }

    @Test
    void updateAddressFaculty() throws SQLException {
        faculty.updateAddress(1, 1, "Ropar");
        customAssert("SUCCESS: Address updated!\n");
    }

    @Test
    void updatePhoneWrongRole() throws SQLException {
        student.updatePhone(1, 2, "1111111111");
        customAssert("UNSUCCESSFUL ACTION: Invalid role!\n");
    }

    @Test
    void updateAddressWrongRole() throws SQLException {
        student.updateAddress(1, 2, "Ropar");
        customAssert("UNSUCCESSFUL ACTION: Invalid role!\n");
    }

    //login tests
    @Test
    void loginStudent() throws SQLException {;
        Assertions.assertEquals(1, student.login("pstudent1", "1234", "0"));
    }

    @Test
    void loginFaculty() throws SQLException {
        Assertions.assertEquals(1, faculty.login("pfaculty1", "1234", "1"));
    }

    @Test
    void loginAcadOffice() throws SQLException {
        Assertions.assertEquals(1, acadOffice.login("pacad1", "1234", "2"));
    }

    @Test
    void loginWrongUsername() throws SQLException {
        Assertions.assertEquals(-1, student.login("wrongusername", "1234", "0"));
    }
    @Test
    void loginWrongPassword() throws SQLException {
        Assertions.assertEquals(-1, student.login("pstudent1", "wrongpassword", "0"));
    }
}