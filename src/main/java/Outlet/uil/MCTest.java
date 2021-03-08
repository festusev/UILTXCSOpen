package Outlet.uil;

import Outlet.Countdown;

import java.util.Collection;
import java.util.Set;


/**
 * Can include MC or short text answers
 */
public class MCTest {
    public final boolean exists;    // If this MC test exists and is being run

    public final String NAME = "Written";
    public String[][] KEY; // The answer key. Each problem is a String tuple with the first value being the answer and the second being the answer type ("0","1")
    // The key string cannot be longer than 40 characters
    public short NUM_PROBLEMS;
    public short CORRECT_PTS;   // Number of points for getting a question correct
    public short INCORRECT_PTS;
    public short SKIPPED_PTS = 0;
    public short MAX_POINTS;
    public String TIME_TEXT;
    public String INSTRUCTIONS;   // Multiple Choice instructions
    public final char[] options = new char[]{'a', 'b', 'c', 'd', 'e'};
    public long TIME;  // The length of time in milliseconds that they have to test
    public String TEST_LINK;  // The url to the test
    // public String ANSWERS;    // Either a url to an answer packet or a text list of the answers for each question.
    // public boolean ANSWERS_LINK;  // True if the ANSWERS variable is a link

    public Countdown opens; // The time that this opens
    public Countdown closes;

    public static String SKIP_CODE = "jieKYL";  // The code entered to signify a skipped problem

    public MCTest() {
        exists = false;KEY= new String[0][];NUM_PROBLEMS = 0; CORRECT_PTS =0;INCORRECT_PTS=0;
        TIME_TEXT = ""; INSTRUCTIONS = ""; TIME = 0; MAX_POINTS=0;TEST_LINK="";/*ANSWERS="";ANSWERS_LINK=false;*/
    }


    public MCTest (boolean published, String opensString, String[][] key, short c, short incorrectPoints, String instructions,
                   String testLink, long time) {
        if(published) {
            opens = new Countdown(opensString, "countdown");NUM_PROBLEMS = (short)key.length; CORRECT_PTS = c; INCORRECT_PTS = incorrectPoints; exists = true;
            TIME_TEXT = (time/(1000*60)) + " minutes";INSTRUCTIONS = instructions;TEST_LINK=testLink;this.TIME = time;MAX_POINTS = (short)(NUM_PROBLEMS*CORRECT_PTS);

            KEY = key;

            /*if(answers.isEmpty()) { // In this case, we generate a text list of the answers
                answers="";
                ANSWERS_LINK=false;
                for(int counter=1; counter<=KEY.length;counter++) {
                    answers+=counter+". " + KEY[counter-1]+" ";
                }
            } else {
                ANSWERS_LINK=true;
            }*/
            // ANSWERS=answers;

            closes = Countdown.add(opens, TIME, "countdown");
        } else {
            if (opensString == null) opensString = "";
            if (key == null) key = new String[][]{};
            opens = new Countdown(opensString);
            NUM_PROBLEMS = (short) key.length;
            CORRECT_PTS = c;
            INCORRECT_PTS = incorrectPoints;
            exists = true;
            TIME_TEXT = (time / (1000 * 60)) + " minutes";
            INSTRUCTIONS = instructions;
            TEST_LINK = testLink;
            this.TIME = time;
            MAX_POINTS = (short) (NUM_PROBLEMS * CORRECT_PTS);

            KEY = key;

            /*if (answers == null || answers.isEmpty()) { // In this case, we generate a text list of the answers
                answers = "";
                ANSWERS_LINK = false;
                for (int counter = 1; counter <= KEY.length; counter++) {
                    answers += counter + ". " + KEY[counter - 1][0] + " ";
                }
            } else {
                ANSWERS_LINK = true;
            }
            ANSWERS = answers;*/

            closes = new Countdown(opensString);
        }
    }

    public short[] score(String[] answers){
        if(answers.length != KEY.length) return new short[]{0,0,0,0};

        short[] report = new short[4];
        for(int i=0; i<NUM_PROBLEMS;i++) {
            try{    /* If the answer is numeric, treat it so */
                double key = Double.parseDouble(KEY[i][0]);
                double answer = Double.parseDouble(answers[i]);

                if(key == answer) {
                    report[1] += 1;
                } else {
                    report[3] += 1;
                }
            } catch(NumberFormatException e) {
                if (answers[i].equals(KEY[i][0])) {
                    report[1] += 1;
                } else if (answers[i].equals(SKIP_CODE)) {
                    report[2] += 1;
                } else {
                    report[3] += 1;
                }
            }
        }
        report[0] = (short)(report[1]*Math.abs(CORRECT_PTS) + report[2]*SKIPPED_PTS + -1*report[3]*Math.abs(INCORRECT_PTS));
        return report;
    }

    /***
     * Takes in a mcIndices from an old MCTest.mcIndices is an array of indices, where each member represents
     * the problem's old index. Deletes mc problems from submissions or regrades them.
     * This also updates all UILEntrys' mcSubmissions that are signed up for this competition.
     * oldNumProblems is the number of problems that used to exist. The difference between this and the length of mcIndices
     * is used to delete problems.
     * @param mcIndices
     * @param oldNumProblems
     * @param competition
     */
    public void updateSubmissions(short[] mcIndices, int oldNumProblems, Competition competition) {
        for(UILEntry entry: competition.entries.allEntries) {
            Set<Short> uids = entry.mc.keySet();
            for(short uid: uids) {
                MCSubmission oldSubmission = entry.mc.get(uid);
                if(oldSubmission == null) continue;

                String[] newAnswers = new String[mcIndices.length];
                for(int i=0;i<mcIndices.length;i++) {
                    // If greater than or equal to 0, index is the old index of this problem. If it is new, it is -1.
                    short index = mcIndices[i];
                    if(index >= 0) newAnswers[i] = oldSubmission.answers[index];
                    else newAnswers[i] = SKIP_CODE;
                }
                MCSubmission newSubmission = new MCSubmission(newAnswers, this.score(newAnswers), true);
                entry.mc.put(uid, newSubmission);
            }
            entry.update();
        }
    }

    public Countdown getTimer() {
        Countdown timer = new Countdown(TIME, opens.date.getTime(), "mcTestTimer");
        timer.onDone = "submitMC(function(){location.reload();})";
        String str = timer.toString();
        return timer;
    }
}
