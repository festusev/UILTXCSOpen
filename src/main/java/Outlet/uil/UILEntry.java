package Outlet.uil;

import Outlet.Countdown;
import Outlet.challenge.Challenge;
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
            } else if(cid == 3) {
                return MathEntry.loadEntry(tid);
            } else if(cid == 4) {
                return NumberSenseEntry.loadEntry(tid);
            } else if(cid == 5) {
                return CalcAppEntry.loadEntry(tid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns true if the competition 'cid' is currently running.
     * @param cid
     * @return
     */
    public static boolean compRunning(short cid) {
        Template template = getTemplate(cid);

        return template.opens.done() && !template.closes.done();
    }

    public static Template getTemplate(short cid) {
        if(cid==1) {
            return CS.template;
        }
        else if(cid == 2) {
            return Challenge.template;
        } else if(cid == 3) {
            return Mathematics.template;
        } else if(cid == 4) {
            return NumberSense.template;
        } else if(cid == 5) {
            return CalculatorApplications.template;
        } else {
            return null;
        }
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

