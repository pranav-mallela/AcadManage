package org.exceptions.graduation;

public class coreCoursesNotCompletedException extends Exception{
    public coreCoursesNotCompletedException(String studentId)
    {
        super(String.format("Student with id %s has not completed all core courses!", studentId));
    }
}
