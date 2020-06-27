package Outlet.uil;

import Outlet.Team;

import java.util.Comparator;

public abstract class SortUILTeams implements Comparator<Team> {
    abstract public int compare(Team t1, Team t2);
}
