package Outlet.uil;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    enum MCAnswer {
        CORRECT(0),
        INCORRECT(1),
        SKIPPED(2);

        private int value;
        private static HashMap map = new HashMap<>();

        private MCAnswer(int value) {
            this.value = value;
        }

        static {
            for (MCAnswer pageType : MCAnswer.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static MCAnswer valueOf(int pageType) {
            return (MCAnswer) map.get(pageType);
        }

        public int getValue() {
            return value;
        }
    }
    public Pair<String, MCAnswer>[] answers;
    public short[] scoringReport;
    public boolean finished;

    private static Gson gson = new Gson();
    public MCSubmission(int numProblems, boolean finished){
        answers = new Pair[numProblems];
        Arrays.fill(answers, new Pair<String, MCAnswer>("", MCAnswer.SKIPPED));
        scoringReport=new short[4];
        this.finished = finished;
    }
    public MCSubmission(Pair<String, MCAnswer>[] responses, boolean finished) {
        this.answers = responses;
        this.finished = finished;
    }

    public JsonArray serialize() {
        JsonArray ret = new JsonArray();

        JsonArray responses = new JsonArray();
        for(Pair<String,MCAnswer> answer: answers) {
            JsonArray response = new JsonArray();
            response.add(answer.key);
            response.add(answer.value.getValue());
            responses.add(response);
        }
        ret.add(responses);

        ret.add(finished);

        return ret;
    }

    public static MCSubmission deserialize(JsonArray data, Competition competition){
        JsonArray responsesTemp = data.get(0).getAsJsonArray();
        if(data.size() == 3) {  // This is the old database format
            Pair<String, MCAnswer>[] responses = new Pair[responsesTemp.size()];
            for (int i = 0, j = responsesTemp.size(); i < j; i++) {
                Pair<String, MCAnswer> pair = new Pair<String, MCAnswer>(responsesTemp.get(i).getAsString(), MCAnswer.SKIPPED);
                responses[i] = pair;
            }

            JsonArray scoringReportTemp = data.get(1).getAsJsonArray();
            short[] scoringReport = new short[4];
            for(int i=0,j=scoringReportTemp.size(); i<j; i++){
                scoringReport[i] = scoringReportTemp.get(i).getAsShort();
            }

            MCSubmission submission = new MCSubmission(responses, Boolean.parseBoolean(data.get(2).getAsString()));
            submission.scoringReport = competition.template.mcTest.score(responses);
            submission.scoringReport = scoringReport;
            return submission;
        } else {
            Pair<String, MCAnswer>[] responses = new Pair[responsesTemp.size()];
            for (int i = 0, j = responsesTemp.size(); i < j; i++) {
                JsonArray response = responsesTemp.get(i).getAsJsonArray();

                Pair<String, MCAnswer> pair = new Pair<String, MCAnswer>(response.get(0).getAsString(), MCAnswer.valueOf(response.get(1).getAsInt()));
                responses[i] = pair;
            }

            MCSubmission submission = new MCSubmission(responses, Boolean.parseBoolean(data.get(1).getAsString()));
            submission.scoringReport = competition.template.mcTest.getScoringReportAndUpdateStats(responses);
            return submission;
        }
    }
}
