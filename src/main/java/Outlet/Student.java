package Outlet;

import Outlet.uil.UILEntry;

import java.util.HashMap;
import java.util.Set;

public class Student extends User{
    public HashMap<Short, UILEntry> cids = new HashMap<>();    // Maps the cid of each competition they are competing in to the UILEntry of their team in that competition
    public short teacherId;
    public Student() {
        this.teacher = false;
    }
    public void setCids(String s) {
        System.out.println("CIDS= "+ s);
        s = s.substring(s.indexOf("[")+1, s.indexOf("]"));
        String[] dict = s.split(",");   // formatted like ['1:2', '3:49']

        cids = new HashMap<>();
        for(String entry: dict) {
            short cid = Short.parseShort(entry.substring(0, entry.indexOf(":")));
            short tid = Short.parseShort(entry.substring(entry.indexOf(":")+1), entry.length());
            cids.put(cid, UILEntry.loadUILEntry(tid, cid));
        }
    }
}
