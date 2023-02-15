package org.connection;

import org.users.AcadOffice;
import org.users.Student;
import org.users.Faculty;

import java.util.ArrayList;
import java.util.Scanner;

public class UI {
    public ArrayList<String> getCredentials()
    {
        ArrayList<String> credentials = new ArrayList<String>();
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome to AcadManage! Please login before continuing.");
        System.out.print("Press:\n0 if you are a student\n1 if you are a faculty\n2 if you are the acad office\n");
        String role = s.nextLine();
        System.out.print("Username: ");
        credentials.add(s.nextLine());
        System.out.print("Password: ");
        credentials.add(s.nextLine());
        credentials.add(role);
        return credentials;
    }

    public void studentMenu(Student user)
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome student!");
        System.out.print("Press:\n1 to add a course\n2 to drop a course\n3 to view enrolled course details\n4 to view your CGPA\n5 to view your most recent SGPA\n");
        int chosenOption = s.nextInt();
        s.nextLine();
        switch (chosenOption) {
            case 1 -> {
                System.out.print("Year for which you are enrolling: ");
                int year = s.nextInt();
                System.out.print("Semester for which you are enrolling: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.addCourse(courseCode, year, semester);
            }
            case 2 -> {
                System.out.print("Course code of the course to be dropped: ");
                String dropCourseCode = s.nextLine();
                user.dropCourse(dropCourseCode);
            }
            case 3 -> user.viewEnrolledCourseDetails();
            case 4 -> {
                System.out.println(String.format("Your CGPA so far is = %f", user.CGPA()));
            }
        }

    }

    public void facultyMenu(Faculty user)
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome faculty!");
        System.out.print("Press:\n1 to float a course\n2 to cancel an offering\n3 to upload grades for an offering\n");
        int chosenOption = s.nextInt();
        switch(chosenOption)
        {
            case 1:
                System.out.print("Year for which you are offering: ");
                int year = s.nextInt();
                System.out.print("Semester for which you are offering: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.floatCourse(courseCode, year, semester);
                break;
            case 2:
                System.out.print("Year for which you wish to cancel offering: ");
                int cancelYear = s.nextInt();
                System.out.print("Semester for which you wish to cancel offering: ");
                int cancelSemester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String cancelCourseCode = s.nextLine();
                user.cancelOffering(cancelCourseCode, cancelYear, cancelSemester);
            case 3:
                // export csv
                System.out.print("Year for which you wish to upload grades: ");
                int uploadYear = s.nextInt();
                System.out.print("Semester for which you wish to upload grades: ");
                int uploadSemester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String uploadCourseCode = s.nextLine();
                user.uploadGrades(uploadCourseCode, uploadYear, uploadSemester);
        }
    }

    public void acadMenu(AcadOffice user)
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome Academic Office!");
        System.out.print("Press:\n1 to add a course to the catalog\n2 to generate a semester transcript of a student\n3 to check for graduation\n");
        int chosenOption = s.nextInt();
        switch(chosenOption)
        {
            case 1:
                System.out.print("Lecture hours per week (l): ");
                int l = s.nextInt();
                System.out.print("Tutorial hours per week (t): ");
                int t = s.nextInt();
                System.out.print("Lab hours per week (p): ");
                int p = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.addCourseToCatalog(courseCode, l, t, p);
                break;
            case 2:
                System.out.print("Enter the student ID of the student for whom you wish to generate a transcript: ");
                int studentId = s.nextInt();
                System.out.print("Year for which you wish to generate a transcript: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to generate a transcript: ");
                int semester = s.nextInt();
                user.generateTranscript(studentId, year, semester);
                System.out.println("\nCheck the directory C:/Users/Public/Transcripts/Student_<studentID>/transcript_<year>_<semester>.txt for the requested transcript.");
        }
    }

}
