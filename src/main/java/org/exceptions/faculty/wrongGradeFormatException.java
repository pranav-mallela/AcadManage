package org.exceptions.faculty;

public class wrongGradeFormatException extends Exception{
    public wrongGradeFormatException()
    {
        super("Grade can only be in a specific format!");
    }
}
