package Outlet;



import Outlet.uil.Competition;
import Outlet.uil.UIL;

import java.util.ArrayList;

public class Teacher extends User{
    public ArrayList<Short> cids = new ArrayList<>();
    public String classCode;
    public Teacher() {
        this.teacher = true;
    }
    public ArrayList<Competition> getCompetitions() {
        ArrayList<Competition> competitions = new ArrayList<>();
        for(short c: cids) {
            competitions.add(UIL.getCompetition(c));
        }
        return competitions;
    }
}
