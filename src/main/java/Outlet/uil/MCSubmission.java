package Outlet.uil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

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

    public JsonArray serialize() {
        JsonArray ret = new JsonArray();

        JsonArray responses = new JsonArray();
        for(String answer: answers) responses.add(answer);
        ret.add(responses);

        JsonArray scoring = new JsonArray();
        for(short s: scoringReport) scoring.add(s);
        ret.add(scoring);

        ret.add(finished);

        return ret;
    }

    public static MCSubmission deserialize(JsonArray data){
        JsonArray responsesTemp = data.get(0).getAsJsonArray();
        String[] responses = new String[responsesTemp.size()];
        for(int i=0,j=responsesTemp.size(); i<j;i++){
            responses[i] = responsesTemp.get(i).getAsString();
        }

        JsonArray scoringReportTemp = data.get(1).getAsJsonArray();
        short[] scoringReport = new short[4];
        for(int i=0,j=scoringReportTemp.size(); i<j; i++){
            scoringReport[i] = scoringReportTemp.get(i).getAsShort();
        }

        return new MCSubmission(responses, scoringReport, Boolean.parseBoolean(data.get(2).getAsString()));
    }
}
