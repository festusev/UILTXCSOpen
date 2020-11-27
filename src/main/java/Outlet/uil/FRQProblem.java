package Outlet.uil;

import com.google.gson.*;

import java.lang.reflect.Type;

public class FRQProblem {
    public String name = "";
    public boolean input = false;
    public boolean output = false;

    private static Gson gson = new Gson();

    public FRQProblem() {
        // Empty
    }

    public FRQProblem(String name, boolean input, boolean output) {
        this.name = name; this.input = input; this.output = output;
    }

    static FRQProblem[] fromJsonArray(String s) {
        System.out.println("Converting from json to array");

        String[][] jsonArray = gson.fromJson(s, String[][].class);

        FRQProblem[] problems = new FRQProblem[jsonArray.length];

        for(int i=0,j=jsonArray.length;i<j;i++) {
            problems[i] = new FRQProblem(jsonArray[i][0], Boolean.parseBoolean(jsonArray[i][1]), Boolean.parseBoolean(jsonArray[i][2]));
            System.out.println("Problem:"+problems[i].name+","+problems[i].input+","+problems[i].output);
        }

        return problems;
    }
}