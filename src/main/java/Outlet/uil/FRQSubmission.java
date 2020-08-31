package Outlet.uil;

public class FRQSubmission {
    public String input;
    public String output;

    enum Result {
        COMPILETIME_ERROR,
        RUNTIME_ERROR,
        SERVER_ERROR,
        WRONG_ANSWER,
        RIGHT_ANSWER,
        EMPTY_FILE,
        EXCEEDED_TIME_LIMIT,
        UNCLEAR_FILE_TYPE   // When their file name doesn't have a '.' in it
    }

    public Result result;
    short problemNumber;
    UILEntry entry;
    boolean overrideShowOutput = false; // Whether we should overwrite the output of showOutput() with overriddenShowOutput.
    boolean overriddenShowOutput = true;    // This is used in case the teacher changes the judgement but it was initially wrong.

    public FRQSubmission(short problemNumber, Result result, String input, String output) {
        this.problemNumber = problemNumber;
        this.result = result;
        this.input = input;
        this.output = output;
    }

    public boolean takePenalty() {
        return result != Result.RIGHT_ANSWER && result != Result.UNCLEAR_FILE_TYPE && result != Result.SERVER_ERROR;
    }

    public boolean showInput() {
        if(result == Result.EMPTY_FILE) return false;
        return true;
    }

    public boolean showOutput() {
        if(overrideShowOutput) return overriddenShowOutput;
        if(result == Result.WRONG_ANSWER || result == Result.RIGHT_ANSWER) return true;
        return false;
    }

    public String getResultString() {
        switch (result) {
            case COMPILETIME_ERROR:
                return "Compile time Error";
            case RUNTIME_ERROR:
                return "Runtime Error";
            case WRONG_ANSWER:
                return "Wrong Answer";
            case RIGHT_ANSWER:
                return "Right Answer";
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
        return !takePenalty() && result != Result.RIGHT_ANSWER;
    }

    public String getCondensedResult() {
        if(takePenalty()) return "Incorrect";
        else if(result == Result.RIGHT_ANSWER) return "Correct";
        else return "No Penalty";
    }
}
