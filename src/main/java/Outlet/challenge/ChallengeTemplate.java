package Outlet.challenge;

import Outlet.*;
import Outlet.uil.*;

import java.util.*;

public class ChallengeTemplate extends Template {
    public ChallengeTemplate(String name, String whatItIs, String rules, String practice, Countdown opens, Countdown closes, short cid, SortUILTeams sorter){
        super(name, whatItIs, rules, practice, new MCTest(),new FRQTest("", "", (short)0, (short)0, (short)0, new String[0], name, "", 0, new String[0]),opens, closes, cid, sorter);
    }

    @Override
    public String getColumnsHTML(User uData, int competeStatus){
        // First, we determine whether to put a "Sign Up" button, a message saying "Your team is signed up for this
        // competition", a message saying "You must belong to a team to sign up", or a message saying
        // "you must be logged in to sign up for this competition"
        String actMessage = "<button id='signUp' onclick='signUp()'>Sign Up</button>";
        if(competeStatus == 1){
            actMessage = "<p class='subtitle'>Log in to compete</p>";
        } else if(competeStatus == 2) {
            actMessage = "<p class='subtitle'>Join a team to compete</p>";
        } else if(competeStatus == 0) { // If they are already signed up for this competition
            actMessage = "<p class='subtitle'>Your team has signed up for this competition</p>";
        }
        String about = "<div class='column' id='aboutColumn'>" +
                "<div class='row secRow'>" +
                "<h1 id='compName'>" + name + "</h1>" +
                actMessage + "" +
                "</div>" +
                "<div class='row secRow'>" +
                "<h2 class='secHead'>What it is</h2>" +
                "<p class='secBody'><br>" + whatItIs + "</p>" +
                "</div>" +
                "<div class='row secRow'>" +
                "<h2 class='secHead'>Rules</h2>" +
                "<p class='secBody'><br>" + rules + "</p>" +
                "</div>" +
                "<div class='row secRow'>" +
                "<h2 class='secHead'>Practice</h2>" +
                "<p class='secBody'><br>" + practice + "</p>" +
                "</div>" +
                "</div>";
        return about + scoreboardHTML + getFRQHTML(uData, competeStatus);
    }
    public String getFRQHTML(User u, int competeStatus) {
        if(competeStatus == 1) {
            return  "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must be logged in to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 2) {
            return "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>You must belong to a team to compete</h1>" +
                    "</div>";
        } else if(competeStatus == 3) {
            return "<div id='frqColumn' class='column' style='display:none;'>" +
                    "<h1 class='forbiddenPage'>Sign up for this competition to compete</h1>" +
                    "<p class='subtitle' onclick='showAbout()' style='cursor:pointer'>Sign up in the <b>About</b> page</p>" +
                    "</div>";
        }

        ChallengeEntry entry = (ChallengeEntry) u.team.comps.get(cid);
        if(closes.done()) {
            return getFinishedFRQ(entry);
        } else if(opens.done()) {
            return getRunningFRQ(entry);
        } else {
            return "";
        }
    }
    public String getRunningFRQ(ChallengeEntry entry){
        return "<script>grabFRQProblemsTimer = setInterval(function() {grabFRQProblems()}, 1000*10);</script>" +
                "<div id='frqColumn' class='column' style='display:none'><div id='frqSelection'><p id='frqSubmitHeader'>Challenge</p>" +
                "<p id='frqInst'>Choose an output file to submit:</p>" +
                "<form id='submit' onsubmit='submitChallenge(); return false;' enctype='multipart/form-data'><input type='file' id='frqTextfile' accept='.7z'/><button id='submitBtn' class='chngButton'>Submit</button></form></div>"+
                getFRQProblems(entry)+"</div>";
    }
    public String getFinishedFRQ(ChallengeEntry entry){
        return "<div id='frqColumn' class='column' class='column'>"+getFRQProblems(entry)+"</div>";
    }
    public String getFRQProblems(ChallengeEntry entry) {
        return "<div id='frqProblems'><h1>Scoring<br>Districts Won: "+entry.won+"<br>Locality: "+entry.locality+"</h1></div>";
    }
    public void updateScoreboard(){
        ArrayList<Team> teams = ChallengeEntry.getAllEntries();
        Collections.sort(teams, sorter);

        // The table row list of teams in order of points
        String teamList = "";
        int rank = 1;
        for(Team t: teams) {
            ChallengeEntry entry = (ChallengeEntry)t.comps.get(cid);
            teamList+="<tr><td>" + rank + "</td><td>" + t.tname + "</td><td class='leastImportant'>" + t.affiliation + "</td><td>" + entry.won +
                    "</td><td class='leastImportant'>" + entry.locality+"</td></tr>";
            rank ++;
        }

        // create HTML
        scoreboardHTML = "<div class='column' id='scoreboardColumn' style='display:none;'>" +
                "<table id='teamList'><tr><th>#</th><th>Team</th><th class='leastImportant' class='right'>School</th>" +
                "<th>Districts Won</th><th class='leastImportant' class='right'>Locality</th></tr></tr>" + teamList + "</table></div>";
    }
}
