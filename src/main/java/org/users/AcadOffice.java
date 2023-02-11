package org.users;

import java.sql.Connection;

public class AcadOffice extends User{
    public AcadOffice(Connection conn) {
        super(conn);
    }

    public void generateTranscript(String rollNo, int year, int semester)
    {
        // print transcript
    }

    public boolean gradCheck(String rollNo)
    {
        // wish life were this easy
        return true;
    }
}
