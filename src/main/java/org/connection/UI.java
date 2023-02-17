package org.connection;

import org.users.AcadOffice;
import org.users.Student;
import org.users.Faculty;

import java.sql.Array;
import java.util.*;
import java.util.ArrayList;
import java.util.Scanner;

public class UI {
    public ArrayList<String> getCredentials()
    {
        ArrayList<String> credentials = new ArrayList<String>();
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome to AcadManage! Please login before continuing.");
        System.out.print("Press:\n0 if you are a Student\n1 if you are a Faculty\n2 if you are the Academic Office\n");
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
        System.out.print("Press:\n1 to float a course\n2 to cancel an offering\n3 to upload grades for an offering\n4 to view grades in a particular offering\n5 to add constraints to an offering\n");
        int chosenOption = s.nextInt();
        switch (chosenOption) {
            case 1 -> {
                System.out.print("Year for which you are offering: ");
                int year = s.nextInt();
                System.out.print("Semester for which you are offering: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.floatCourse(courseCode, year, semester);
            }
            case 2 -> {
                System.out.print("Year for which you wish to cancel offering: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to cancel offering: ");
                int semester = s.nextInt();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.cancelOffering(courseCode, year, semester);
            }
            case 3 -> {
                // export csv
                System.out.print("Year for which you wish to upload grades: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to upload grades: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.uploadGrades(courseCode, year, semester);
            }
            case 4 -> {
                // ask for year, sem, and course_code
                System.out.print("Year for which you wish to view grades: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to view grades: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                user.viewGrades(year, semester, courseCode);
            }
            case 5 -> {
                System.out.print("Year for which you wish to add constraints: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to add constraints: ");
                int semester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                System.out.print("Enter the course codes of pre-requisites for this course (space separated): ");
                String [] preReqs = s.nextLine().split(" ");
                List<List<List<String>>> optionPreReqs = new ArrayList<>();
                if(!preReqs[0].equals(""))
                {
                    for (String preReq : preReqs) {
                        System.out.printf("Enter the courses equivalent to %s that will be also be considered (space separated): ", preReq);
                        List<List<String>> orPreReqGrades = new ArrayList<>();
                        List<String> orPreReqs = new ArrayList<>();
                        String[] optionalPreReq = s.nextLine().split(" ");
                        if (!optionalPreReq[0].equals("")) {
                            orPreReqs.addAll(Arrays.asList(optionalPreReq));
                        }
                        orPreReqs.add(preReq);
                        for (String orPreReq : orPreReqs) {
                            System.out.printf("Enter the minimum grade required in %s (default is D): ", orPreReq);
                            String grade = s.nextLine();
                            if (grade.equals("")) grade = "D";
                            orPreReqGrades.add(List.of(new String[]{orPreReq, grade}));
                        }
                        optionPreReqs.add(orPreReqGrades);
                    }
                }
//                System.out.println(optionPreReqs);
                user.addConstraintsToOffering(year, semester, courseCode, optionPreReqs);
            }
        }
    }

    public void acadMenu(AcadOffice user)
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Welcome Academic Office!");
        System.out.print("Press:\n1 to add a course to the catalog\n2 to generate a semester transcript of a student\n3 to view grades of a student\n4 to view grades of an offering\n");
        int chosenOption = s.nextInt();
        switch (chosenOption) {
            case 1 -> {
                System.out.print("Lecture hours per week (l): ");
                int l = s.nextInt();
                System.out.print("Tutorial hours per week (t): ");
                int t = s.nextInt();
                System.out.print("Lab hours per week (p): ");
                int p = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String courseCode = s.nextLine();
                System.out.print("Enter the course codes of pre-requisites for this course (space separated): ");
                String [] preReqs = s.nextLine().split(" ");
                List<List<String>> optionPreReqs = new ArrayList<>();
                if(!preReqs[0].equals(""))
                {
                    for(int i=0;i<preReqs.length;i++)
                    {
                        System.out.print(String.format("Enter the courses equivalent to %s that will be also be considered (space separated): ", preReqs[i]));
                        List<String> orPreReqs = new ArrayList<>();
                        String [] optionalPreReq = s.nextLine().split(" ");
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
                user.addCourseToCatalog(courseCode, l, t, p, optionPreReqs);
            }
            case 2 -> {
                System.out.print("Enter the student ID of the student for whom you wish to generate a transcript: ");
                int studentId = s.nextInt();
                System.out.print("Year for which you wish to generate a transcript: ");
                int year = s.nextInt();
                System.out.print("Semester for which you wish to generate a transcript: ");
                int semester = s.nextInt();
                user.generateTranscript(studentId, year, semester);
                System.out.println("\nCheck the directory C:/Users/Public/Transcripts/Student_<studentID>/transcript_<year>_<semester>.txt for the requested transcript.");
            }
            case 3 -> {
                System.out.print("Enter the student ID to view the corresponding student's grades: ");
                int studentId = s.nextInt();
                user.viewStudentGrades(studentId);
            }

            case 4 -> {
                System.out.print("Year for which you wish to view grades: ");
                int viewYear = s.nextInt();
                System.out.print("Semester for which you wish to view grades: ");
                int viewSemester = s.nextInt();
                s.nextLine();
                System.out.print("Enter the course code: ");
                String viewCourseCode = s.nextLine();
                user.viewOfferingGrades(viewYear, viewSemester, viewCourseCode);
            }
        }
    }

}
