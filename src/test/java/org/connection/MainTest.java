package org.connection;

import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    static ByteArrayOutputStream baos;

    @BeforeAll
    static void mainSetUp() {
        baos = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
    }
    @BeforeEach
    public void reset() {
        baos.reset();
    }

    public void simulateInput(String input) {
        InputStream in = new ByteArrayInputStream((input+"\n").getBytes());
        System.setIn(in);
    }

    public void customAssert(String expected) {
        String output = baos.toString();
        Assertions.assertEquals(expected, output);
    }

    @Test
    void validCredentialsStudent() {
        simulateInput("0\npstudent1\n1234");
        Main.main(null);
    }

    @Test
    void validCredentialsFaculty() {
        simulateInput("1\npfaculty1\n1234");
        Main.main(null);
    }

    @Test
    void validCredentialsAcadOffice() {
        simulateInput("2\npacad1\n1234");
        Main.main(null);
    }

    @Test
    void invalidCredentials() {
        simulateInput("0\npstudent1\n12345\nN");
        Main.main(null);
        Assertions.assertTrue(baos.toString().contains("UNSUCCESSFUL LOGIN: Invalid credentials!"));
    }




}