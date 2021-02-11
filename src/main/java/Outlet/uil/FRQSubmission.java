package Outlet.uil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class FRQSubmission {
    public String input;
    public String output;

    enum Result {
        COMPILETIME_ERROR,
        RUNTIME_ERROR,
        SERVER_ERROR,
        INCORRECT,
        CORRECT,
        EMPTY_FILE,
        EXCEEDED_TIME_LIMIT,
        UNCLEAR_FILE_TYPE   // When their file name doesn't have a '.' in it
    }

    public Result result;
    short problemNumber;
    long submittedTime;  // Time in milliseconds when it was submitted

    UILEntry entry;
    boolean overrideShowOutput = false; // Whether we should overwrite the output of showOutput() with overriddenShowOutput.
    boolean overriddenShowOutput = true;    // This is used in case the teacher changes the judgement but it was initially wrong.

    public FRQSubmission(short problemNumber, Result result, String input, String output, long submittedTime) {
        this.problemNumber = problemNumber;
        this.result = result;
        this.input = input;
        this.output = output;
        this.submittedTime = submittedTime;
    }

    public boolean takePenalty() {
        return result != Result.CORRECT && result != Result.UNCLEAR_FILE_TYPE && result != Result.SERVER_ERROR;
    }

    public boolean showInput() {
        if(result == Result.EMPTY_FILE) return false;
        return true;
    }

    public boolean showOutput() {
        if(overrideShowOutput) return overriddenShowOutput;
        if(result == Result.INCORRECT || result == Result.CORRECT) return true;
        return false;
    }

    public String getResultString() {
        switch (result) {
            case COMPILETIME_ERROR:
                return "Compile time Error";
            case RUNTIME_ERROR:
                return "Runtime Error";
            case INCORRECT:
                return "Incorrect";
            case CORRECT:
                return "Correct";
            case EMPTY_FILE:
                return "Empty File";
            case EXCEEDED_TIME_LIMIT:
                return "Time Limit Exceeded";
            case UNCLEAR_FILE_TYPE:
                return "Unclear File Type";
            case SERVER_ERROR:
            default:
                return "Server Error";
        }
    }

    public boolean noPenalty() {
        return !takePenalty() && result != Result.CORRECT;
    }


    /**
     * The json is in the following format:
     * [
     * [ response number (attempts),
     *   [
     *     [input,
     *     output,
     *     judgement]
     *   ]
     * ], [...], ...]
     * If the judgement dictates it, there will be no output displayed.
     * @param json
     * @return
     */
    public static Pair<Short, ArrayList<FRQSubmission>>[] parseList(String json, UILEntry entry) {
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        Pair<Short, ArrayList<FRQSubmission>>[] problems = new Pair[jsonArray.size()];
        for(short i=0;i<jsonArray.size();i++) {
            JsonArray problem = jsonArray.get(i).getAsJsonArray();
            short responseNumber = problem.get(0).getAsShort();

            JsonArray submissionsJson = problem.get(1).getAsJsonArray();
            ArrayList<FRQSubmission> submissions = new ArrayList<>();
            for(JsonElement submissionT: submissionsJson) {
                JsonArray submissionJson = submissionT.getAsJsonArray();
                String input = submissionJson.get(0).getAsString();
                String output = submissionJson.get(1).getAsString();
                String judgementI = submissionJson.get(2).getAsString();
                Long submittedTime = submissionJson.get(3).getAsLong();

                Result judgement = Result.valueOf(judgementI);
                FRQSubmission submission = new FRQSubmission((short)(i + 1), judgement, input, output, submittedTime);    // Problem indices begin at 1
                submission.entry = entry;
                submissions.add(submission);
            }
            problems[i] = new Pair(responseNumber, submissions);
        }
        return problems;
    }

    public static String stringifyList(Pair<Short, ArrayList<FRQSubmission>>[] list) {
        JsonArray json = new JsonArray();
        for(short i=0;i<list.length;i++) {
            Pair<Short, ArrayList<FRQSubmission>> pair = list[i];
            JsonArray problem = new JsonArray();
            problem.add(pair.key);

            JsonArray submissionsJson = new JsonArray();
            for(FRQSubmission submission: pair.value) {
                JsonArray submissionJson = new JsonArray();
                submissionJson.add(submission.input);
                submissionJson.add(submission.output);
                submissionJson.add(submission.result.name());
                submissionJson.add(submission.submittedTime);

                submissionsJson.add(submissionJson);
            }
            problem.add(submissionsJson);

            json.add(problem);
        }
        return json.toString();
    }
}
