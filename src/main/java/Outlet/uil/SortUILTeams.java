package Outlet.uil;

import java.util.Comparator;

public class SortUILTeams implements Comparator<UILEntry> {
    public int compare(UILEntry t1, UILEntry t2) {
        int t2Score = t2.getScore();
        int t1Score = t1.getScore();
        if(t2Score == t1Score) {
            Competition competition = t1.competition;
            if(competition.template.frqTest.exists) {   // First, compare their frq scores
                int t2FRQ = t2.frqScore;
                int t1FRQ = t1.frqScore;
                if(t2FRQ != t1FRQ) return t2FRQ - t1FRQ;
            }
            // Next, compare their written scores including the alternate
            if(competition.template.mcTest.exists) {
                int t2MC = 0;
                for(short i: t2.mc.keySet()){
                    MCSubmission submission = t2.mc.get(i);
                    if(submission != null) t2MC += submission.scoringReport[0];
                }
                int t1MC = 0;
                for(short i: t1.mc.keySet()){
                    MCSubmission submission = t1.mc.get(i);
                    if(submission != null) t1MC += submission.scoringReport[0];
                }
                if(t2MC != t1MC) return t2MC - t1MC;
            }
            return 0;   // There is a tie
        } else return t2.getScore() - t1.getScore();
    }
}
