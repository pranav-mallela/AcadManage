package org.exceptions.graduation;

public class electiveCoursesNotCompletedException extends Exception{
    public electiveCoursesNotCompletedException(String studentId)
    {
        super(String.format("Student with id %s has not completed the required elective courses!", studentId));
    }
}
