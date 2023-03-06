package org.connection;

import org.scanners.CustomScanner;
import org.users.AcadOffice;
import org.users.Student;
import org.users.Faculty;

import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;
import java.util.Scanner;

public class UI {
    public ArrayList<String> getCredentials()
    {
        ArrayList<String> credentials = new ArrayList<String>();
        CustomScanner s = new CustomScanner();
        System.out.print("Welcome to AcadManage! Please login before continuing.\n");
        System.out.print("Press:\n[0] if you are a Student\n[1] if you are a Faculty\n[2] if you are the Academic Office\n");
        String role = s.getString();
        System.out.print("Username: ");
        credentials.add(s.getString());
        System.out.print("Password: ");
        credentials.add(s.getString());
        credentials.add(role);
        return credentials;
    }

    public void studentMenu(Student user) throws Exception
    {
        CustomScanner s = new CustomScanner();
        System.out.print("Welcome student!\n");
        int chosenOption = -1;
        while(chosenOption != 0)
        {
            System.out.print("Press:\n[0] to logout\n[1] to add a course\n[2] to drop a course\n[3] to view enrolled course details\n[4] to view your CGPA\n[5] to update your phone\n[6] to update your address\n");
            chosenOption = s.getInt();
            s.getString();
            switch (chosenOption) {
                case 1 -> {
                    System.out.print("Year for which you are enrolling: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you are enrolling: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    user.addCourse(courseCode, year, semester);
                }
                case 2 -> {
                    System.out.print("Course code of the course to be dropped: ");
                    String dropCourseCode = s.getCourse();
                    user.dropCourse(dropCourseCode);
                }
                case 3 -> user.viewEnrolledCourseDetails();
                case 4 -> {
                    System.out.printf("Your CGPA so far is = %f%n", user.CGPA());
                }
                case 5 -> {
                    System.out.print("Enter your phone number: ");
                    String newPhone = s.getPhone();
                    user.updatePhone(user.studentId, 0, newPhone);
                }
                case 6 -> {
                    System.out.print("Enter your address: ");
                    String newAddress = s.getString();
                    user.updateAddress(user.studentId, 0, newAddress);
                }
            }
        }

    }

    public void facultyMenu(Faculty user) throws SQLException
    {
        CustomScanner s = new CustomScanner();
        System.out.print("Welcome faculty!");
        int chosenOption = -1;
        while(chosenOption != 0)
        {
            System.out.print("Press:\n[0] to logout\n[1] to float a course\n[2] to cancel an offering\n[3] to upload grades for an offering\n[4] to view grades in a particular offering\n[5] to add pre-requisite constraints to an offering\n[6] to add CGPA constraints to an offering\n[7] to update your phone\n[8] to update your address\n");
            chosenOption = s.getInt();
            switch (chosenOption) {
                case 1 -> {
                    System.out.print("Year for which you are offering: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you are offering: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    user.floatCourse(courseCode, year, semester);
                }
                case 2 -> {
                    System.out.print("Year for which you wish to cancel offering: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to cancel offering: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    user.cancelOffering(courseCode, year, semester);
                }
                case 3 -> {
                    // export csv
                    System.out.print("Year for which you wish to upload grades: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to upload grades: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    user.uploadGrades(courseCode, year, semester, s);
                }
                case 4 -> {
                    // ask for year, sem, and course_code
                    System.out.print("Year for which you wish to view grades: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to view grades: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    user.viewGrades(year, semester, courseCode);
                }
                case 5 -> {
                    System.out.print("Year for which you wish to add constraints: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to add constraints: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    System.out.print("Enter the course codes of pre-requisites for this course (space separated): ");
                    String [] preReqs = s.getString().split(" ");
                    List<List<List<String>>> optionPreReqs = new ArrayList<>();
                    if(!preReqs[0].equals(""))
                    {
                        for (String preReq : preReqs) {
                            System.out.printf("Enter the courses equivalent to %s that will be also be considered (space separated): ", preReq);
                            List<List<String>> orPreReqGrades = new ArrayList<>();
                            List<String> orPreReqs = new ArrayList<>();
                            String[] optionalPreReq = s.getString().split(" ");
                            if (!optionalPreReq[0].equals("")) {
                                orPreReqs.addAll(Arrays.asList(optionalPreReq));
                            }
                            orPreReqs.add(preReq);
                            for (String orPreReq : orPreReqs) {
                                System.out.printf("Enter the minimum grade required in %s (default is D): ", orPreReq);
                                String grade = s.getGrade();
                                if (grade.equals("")) grade = "D";
                                orPreReqGrades.add(List.of(new String[]{orPreReq, grade}));
                            }
                            optionPreReqs.add(orPreReqGrades);
                        }
                    }
                    user.addConstraintsToOffering(year, semester, courseCode, optionPreReqs);
                }
                case 6 -> {
                    System.out.print("Year for which you wish to add constraints: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to add constraints: ");
                    int semester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    System.out.print("Enter the minimum CGPA required for this course: ");
                    float minCGPA = s.getFloat();
                    user.addCGConstraints(year, semester, courseCode, minCGPA);
                }
                case 7 -> {
                    System.out.print("Enter your phone number: ");
                    String newPhone = s.getPhone();
                    user.updatePhone(user.facultyId, 1, newPhone);
                }
                case 8 -> {
                    System.out.print("Enter your address: ");
                    String newAddress = s.getString();
                    user.updateAddress(user.facultyId, 1, newAddress);
                }
            }
        }
    }

    public void acadMenu(AcadOffice user) throws Exception
    {
        CustomScanner s = new CustomScanner();
        System.out.print("Welcome Academic Office!");
        int chosenOption = -1;
        while(chosenOption != 0)
        {
            System.out.print("Press:\n[0] to logout\n[1] to add a course to the catalog\n[2] to generate a semester transcript of a student\n[3] to view grades of a student\n[4] to view grades of an offering\n[5] to check the graduation status of a student\n[6] to set a semester event\n");
            chosenOption = s.getInt();
            switch (chosenOption) {
                case 1 -> {
                    System.out.print("Lecture hours per week (l): ");
                    float l = s.getFloat();
                    System.out.print("Tutorial hours per week (t): ");
                    float t = s.getFloat();
                    System.out.print("Lab hours per week (p): ");
                    float p = s.getFloat();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String courseCode = s.getCourse();
                    System.out.print("Enter the course title: ");
                    String courseTitle = s.getString();
                    System.out.print("Enter the course codes of pre-requisites for this course (space separated): ");
                    String [] preReqs = s.getString().split(" ");
                    List<List<String>> optionPreReqs = new ArrayList<>();
                    if(!preReqs[0].equals(""))
                    {
                        for(int i=0;i<preReqs.length;i++)
                        {
                            System.out.printf("Enter the courses equivalent to %s that will be also be considered (space separated): ", preReqs[i]);
                            List<String> orPreReqs = new ArrayList<>();
                            String [] optionalPreReq = s.getString().split(" ");
                            if(!optionalPreReq[0].equals(""))
                            {
                                for(int j=0;j<optionalPreReq.length;j++)
                                {
                                    orPreReqs.add(optionalPreReq[i]);
                                }
                            }
                            orPreReqs.add(preReqs[i]);
                            optionPreReqs.add(orPreReqs);
                        }
                    }
                    user.addCourseToCatalog(courseCode, l, t, p, optionPreReqs, courseTitle);
                }
                case 2 -> {
                    System.out.print("Enter the student ID of the student for whom you wish to generate a transcript: ");
                    int studentId = s.getInt();
                    System.out.print("Year for which you wish to generate a transcript: ");
                    int year = s.getInt();
                    System.out.print("Semester for which you wish to generate a transcript: ");
                    int semester = s.getInt();
                    user.generateTranscript(studentId, year, semester);
                    System.out.print("\nCheck the directory C:/Users/Public/Transcripts/Student_<studentID>/transcript_<year>_<semester>.txt for the requested transcript.");
                }
                case 3 -> {
                    System.out.print("Enter the student ID to view the corresponding student's grades: ");
                    int studentId = s.getInt();
                    user.viewStudentGrades(studentId);
                }

                case 4 -> {
                    System.out.print("Year for which you wish to view grades: ");
                    int viewYear = s.getInt();
                    System.out.print("Semester for which you wish to view grades: ");
                    int viewSemester = s.getInt();
                    s.getString();
                    System.out.print("Enter the course code: ");
                    String viewCourseCode = s.getCourse();
                    user.viewOfferingGrades(viewYear, viewSemester, viewCourseCode);
                }
                case 5 -> {
                    System.out.print("Enter the student ID to view the corresponding student's graduation status: ");
                    int studentId = s.getInt();
                    boolean canGraduate = user.canGraduate(studentId);
                }
                case 6 -> {
                    System.out.print("Press:\n[1] to set event to 'Before Semester'\n[2] to set event to 'Semester Running'\n[3] to set event to 'After Semester'\n");
                    int action = s.getInt();
                    s.getString();
                    user.setSemesterEvent(action);
                }
            }
        }
    }

}
