package Outlet;

import java.util.TimerTask;

/**
 * Flushes expired rows every 15 minutes
 */
public class VerificationFlusher extends TimerTask {
    public void run(){
        Conn.flushVerificationTable();
    }
}
