package Outlet.uil;

import Outlet.Countdown;

/**
 * Can include MC or short text answers
 */
public class MCTest {
    public final boolean exists;    // If this MC test exists and is being run

    public final String NAME;
    public final String[] KEY; // The answer key
    public final short[] PROBLEM_MAP;   // Indicates whether a question is multiple choice or short answer. If 0, MC, if 1, Short answer
    public final int NUM_PROBLEMS;
    public final int CORRECT_PTS;   // Number of points for getting a question correct
    public final int INCORRECT_PTS;
    public final int SKIPPED_PTS;
    public final int MAX_POINTS;
    public final String TIME_TEXT;
    public final String INSTRUCTIONS;   // Multiple Choice instructions
    public char[] options = new char[]{'a', 'b', 'c', 'd', 'e'};
    public final long TIME;  // The length of time in milliseconds that they have to test

    public static final String SKIP_CODE = "SK";  // The code entered to signify a skipped problem

    public MCTest() {
        exists = false;KEY= new String[0];NUM_PROBLEMS = 0; CORRECT_PTS =0;INCORRECT_PTS=0;SKIPPED_PTS=0;NAME="";
        PROBLEM_MAP = new short[0]; TIME_TEXT = ""; INSTRUCTIONS = ""; TIME = 0; MAX_POINTS=0;
    }

    public MCTest (String[] k, short[] problemMap, int n, int c, int i, int s, String na, String timeText, String instructions, long time) {
        KEY = k; NUM_PROBLEMS = n; CORRECT_PTS = c; INCORRECT_PTS = i; SKIPPED_PTS = s; exists = true; NAME = na;
        PROBLEM_MAP = problemMap;TIME_TEXT = timeText;INSTRUCTIONS = instructions;this.TIME = time;MAX_POINTS = NUM_PROBLEMS*CORRECT_PTS;
    }

    public short[] score(String[] answers){
        if(answers.length != KEY.length) return new short[]{0,0,0,0};

        short[] report = new short[4];
        for(int i=0; i<NUM_PROBLEMS;i++) {
            if(answers[i].equals(KEY[i])) {
                report[1] += 1;
            } else if(answers[i].equals(SKIP_CODE)) {
                report[2] += 1;
            } else {
                report[3] += 1;
            }
        }
        report[0] = (short)(report[1]*CORRECT_PTS + report[2]*SKIPPED_PTS + report[3]*INCORRECT_PTS);
        return report;
    }

    public Countdown getTimer(long started) {
        Countdown timer = new Countdown(TIME, started, "mcTestTimer");
        timer.onDone = "submitMC()";
        return timer;
    }

    public Countdown getTimer(){
        return getTimer(Countdown.getNow());
    }
}