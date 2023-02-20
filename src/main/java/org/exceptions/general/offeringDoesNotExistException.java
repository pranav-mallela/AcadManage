package org.exceptions.general;

public class offeringDoesNotExistException extends Exception{
    public offeringDoesNotExistException(String courseCode)
    {
        super(String.format("Offering for course with code %s does not exist!", courseCode));
    }
}
