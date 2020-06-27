package Outlet.uil;

import Outlet.Countdown;
import Outlet.challenge.ChallengeEntry;

import java.sql.SQLException;
import java.util.*;

public abstract class UILEntry {
    public final short tid;

    // Maps uids to the corresponding MCSubmission
    public HashMap<Short, MCSubmission> mc;

    public UILEntry(short tid){
        this.tid = tid;
    }

    public static UILEntry loadUILEntry(short cid, short tid){
        try{
            if(cid==1) {
                return CSEntry.loadEntry(tid);
            }
            else if(cid == 2) {
                return ChallengeEntry.loadEntry(tid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    abstract public void update();  // Updates entry in the database
    abstract public long beginMC(short uid);    // Returns the time started, even if they have already begun
    abstract public short[] scoreMC(short uid, String[] answers);
    public boolean finishedMC(short uid) {
        if(mc.keySet().contains(uid)) {
            MCSubmission submission = mc.get(uid);
            if(submission.finished >0 || (submission.started+CS.MC_TIME)< Countdown.getNow()) return true;
        }
        return false;
    }
    abstract public int getMCScore();
    abstract public int getScore();
}

