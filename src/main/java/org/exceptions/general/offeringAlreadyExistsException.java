package org.exceptions.general;

public class offeringAlreadyExistsException extends Exception{
    public offeringAlreadyExistsException(String courseCode)
    {
        super(String.format("Offering for course with code %s already exists!", courseCode));
    }
}
