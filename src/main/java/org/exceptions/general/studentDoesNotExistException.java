package org.exceptions.general;

public class studentDoesNotExistException extends Exception{
    public studentDoesNotExistException(String studentId)
    {
        super(String.format("Student with id %s does not exist!", studentId));
    }
}
