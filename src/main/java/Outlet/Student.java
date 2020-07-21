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
        HashMap<String, String> temp = User.gson.fromJson(s, HashMap.class);
        cids = new HashMap<>();
        Set<String> compCids = temp.keySet();
        for(String comp: compCids) {
            short cid = Short.parseShort(comp);
            cids.put(cid, UILEntry.loadUILEntry(Short.parseShort(temp.get(comp)), cid));
        }
    }
}
