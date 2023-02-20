package org.exceptions.student;

public class notPassingConstraintsException extends Exception{
    public notPassingConstraintsException()
    {
        super("You have not passed the set constraints!");
    }
}
