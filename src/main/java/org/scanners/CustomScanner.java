package org.scanners;

import java.util.Scanner;

public class CustomScanner {
    Scanner sc = new Scanner(System.in);
    public int getInt() {
        while(!sc.hasNextInt()) {
            sc.nextLine();
            System.out.print("Enter a valid value: ");
        }
        return sc.nextInt();
    }

    public float getFloat() {
        while(!sc.hasNextFloat()) {
            sc.nextLine();
            System.out.print("Enter a valid value: ");
        }
        return sc.nextFloat();
    }

    public String getCourse() {
        String course = sc.nextLine();
        while(!course.matches("[A-Z]{2}[0-9]{3}")) {
            System.out.print("Enter a valid course code: ");
            course = sc.nextLine();
        }
        return course;
    }

    public String getGrade() {
        String grade = sc.nextLine();
        while(!grade.matches("[A-F]-?|^$|\\\\s+")) {
            System.out.print("Enter a valid grade: ");
            grade = sc.nextLine();
        }
        return grade;
    }

    public String getString() {
        return sc.nextLine();
    }
}
