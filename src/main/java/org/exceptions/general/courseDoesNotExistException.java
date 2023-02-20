package org.exceptions.general;

public class courseDoesNotExistException extends Exception{
    public courseDoesNotExistException(String courseCode)
    {
        super(String.format("Course with code %s does not exist!", courseCode));
    }
}
