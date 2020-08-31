package Outlet.uil;

import java.util.Comparator;

public class SortUILTeams implements Comparator<UILEntry> {
    public int compare(UILEntry t1, UILEntry t2) {
        return t2.getScore() - t1.getScore();
    }
}
