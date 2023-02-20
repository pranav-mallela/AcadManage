package org.exceptions.student;

public class notPassingPreReqsException extends Exception{
    public notPassingPreReqsException(String courseName)
    {
        super(String.format("You have not passed the prerequisites for %s!", courseName));
    }
}
