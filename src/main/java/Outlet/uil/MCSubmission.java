package Outlet.uil;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The scoring report is an array of shorts where the
 *      * first element is the total score,
 *      * second element is the number correct,
 *      * third element is the number skipped
 *      * fourth element is the number incorrect
 * If a user has not yet started the multiple choice then they do not have an entry
 *
 * When stored in the database, formatted like [[response1, response2...],[totalScore, correct...],started, finished]
 */
// A class containing information about a User's MC submission and run
public class MCSubmission{
    public String[] answers;
    public short[] scoringReport;
    public boolean finished;

    private static Gson gson = new Gson();
    public MCSubmission(int numProblems, boolean finished){
        answers = new String[numProblems];
        Arrays.fill(answers, MCTest.SKIP_CODE);
        scoringReport=new short[4];
        this.finished = finished;
    }
    public MCSubmission(String[] responses, short[] scoringReport, boolean finished) {
        this.answers = responses;
        this.scoringReport = scoringReport;
        this.finished = finished;
    }

    public String serialize() {
        String responsesString = gson.toJson(answers);
        System.out.println("RESPONSE STRING: " + responsesString);
        String scoringReportString = gson.toJson(scoringReport);
        String ret = "["+responsesString+","+scoringReportString+",'"+finished+"']";
        return ret.replace("\"","'");
    }

    public static MCSubmission deserialize(ArrayList data){
        Object[] responsesTemp = ((ArrayList)data.get(0)).toArray();
        String[] responses = new String[responsesTemp.length];
        for(int i=0; i<responsesTemp.length;i++){
            responses[i] = (String) responsesTemp[i];
        }
        Object[] scoringReportTemp =((ArrayList)data.get(1)).toArray();
        short[] scoringReport = new short[4];
        for(int i=0; i<scoringReportTemp.length; i++){
            scoringReport[i] = (short)(double)(Double) scoringReportTemp[i];
        }

        return new MCSubmission(responses, scoringReport, Boolean.parseBoolean((String)data.get(2)));
    }
}
