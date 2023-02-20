package org.exceptions.general;

public class wrongSemesterException extends Exception{
    public wrongSemesterException()
    {
        super("Action cannot be performed in this semester");
    }
}
