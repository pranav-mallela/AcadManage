package org.exceptions.graduation;

public class extraCapCoursesNotCompletedException extends Exception{
    public extraCapCoursesNotCompletedException(String studentId)
    {
        super(String.format("Student with id %s has not completed the required extracurricular and capstone courses!", studentId));
    }
}
