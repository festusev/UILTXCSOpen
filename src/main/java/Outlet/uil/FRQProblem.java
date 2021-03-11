package Outlet.uil;

import com.google.gson.*;

import java.lang.reflect.Type;

public class FRQProblem {
    public String name = "";

    public String inputFname = "";
    public String outputFname = "";
    public String outputFile = null;  // Not stored in the database, but if a teacher opens a submission for this problem, it is read from disk and stored here.

    private static Gson gson = new Gson();

    public FRQProblem() {
        // Empty
    }

    public FRQProblem(String name, String inputFname, String outputFname) {
        this.name = name; this.inputFname = inputFname; this.outputFname = outputFname;
    }

    static FRQProblem[] fromJsonArray(String s) {
        JsonArray jsonArray = JsonParser.parseString(s).getAsJsonArray();

        FRQProblem[] problems = new FRQProblem[jsonArray.size()];

        for(int i=0,j=jsonArray.size();i<j;i++) {
            JsonArray problem = jsonArray.get(i).getAsJsonArray();
            problems[i] = new FRQProblem(problem.get(0).getAsString(),
                    problem.get(1).getAsString(), problem.get(2).getAsString());
            System.out.println("Problem:"+problems[i].name+","+problems[i].inputFname+","+problems[i].outputFname);
        }

        return problems;
    }

    static String toJsonArray(FRQProblem[] problems) {
        JsonArray jsonArray = new JsonArray();

        for(int i=0,j=problems.length;i<j;i++) {
            JsonArray jsonProblem = new JsonArray();

            FRQProblem problem = problems[i];
            jsonProblem.add(problem.name);
            jsonProblem.add(problem.inputFname);
            jsonProblem.add(problem.outputFname);
            jsonArray.add(jsonProblem);
        }
        return jsonArray.toString();
    }
}