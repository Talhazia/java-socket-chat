package server;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Debug {
    private final static Boolean isDebugEnabled = true;

    public static void log(String message) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        if(isDebugEnabled)
            System.out.println("[" + sdf.format(cal.getTime()) + "] " + message);
    }
}
