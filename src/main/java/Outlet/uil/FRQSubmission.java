package Outlet.uil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class FRQSubmission {
    public String input;
    public String output;

    public String inputFname;

    enum Result {
        COMPILETIME_ERROR,
        RUNTIME_ERROR,
        SERVER_ERROR,
        INCORRECT,
        CORRECT,
        EMPTY_FILE,
        EXCEEDED_TIME_LIMIT,
        UNCLEAR_FILE_TYPE,   // When their file name doesn't have a '.' in it
        PACKAGE_ERROR,  // When there is still a package statement in their code
        FORMAT_ERROR    // When the output is formatted incorrectly
    }

    // If false, the team will see a "grading" message. If true, they'll see the result.
    // It also won't count as a submission for the purpose of the scoreboard.
    boolean graded;
    public Result result;
    short problemNumber;    // If 0, then this is a dry run submission
    long submittedTime;  // Time in milliseconds when it was submitted

    UILEntry entry;

    public FRQSubmission(short problemNumber, Result result, String inputFname, String input, String output, long submittedTime, boolean graded) {
        this.problemNumber = problemNumber;
        this.result = result;
        this.input = input;
        this.output = output;
        this.submittedTime = submittedTime;
        this.graded = graded;
        this.inputFname = inputFname;
    }

    public boolean takePenalty() {
        return result != Result.CORRECT && result != Result.UNCLEAR_FILE_TYPE && result != Result.SERVER_ERROR && graded;
    }

    public boolean showInput() {
        if(result == Result.EMPTY_FILE) return false;
        return true;
    }

    public boolean showOutput() {
        if(result == Result.EXCEEDED_TIME_LIMIT || result == Result.SERVER_ERROR) return false;
        return true;
    }

    public String getResultString() {
        switch (result) {
            case COMPILETIME_ERROR:
                return "Compile-time Error";
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
                return "File Type Not Allowed";
            case FORMAT_ERROR:
                return "Wrong Output Format";
            case PACKAGE_ERROR:
                return "Package Error";
            case SERVER_ERROR:
            default:
                return "Server Error";
        }
    }


    /**
     * The json is in the following format:
     * [
     * [ response number (attempts),
     *   [
     *     [input,
     *     output,
     *     judgement,
     *     submittedTime,
     *     graded]
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
                boolean graded = submissionJson.get(4).getAsBoolean();
                String inputFname = submissionJson.get(5).getAsString();

                Result judgement = Result.valueOf(judgementI);
                FRQSubmission submission = new FRQSubmission(i, judgement, inputFname, input, output, submittedTime, graded);    // Problem indices begin at 1
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
                submissionJson.add(submission.graded);
                submissionJson.add(submission.inputFname);

                submissionsJson.add(submissionJson);
            }
            problem.add(submissionsJson);

            json.add(problem);
        }
        return json.toString();
    }
}
