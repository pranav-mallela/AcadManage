package org.exceptions.general;

public class courseAlreadyExistsException extends Exception{
    public courseAlreadyExistsException(String courseCode)
    {
        super(String.format("Course with code %s already exists!", courseCode));
    }
}
