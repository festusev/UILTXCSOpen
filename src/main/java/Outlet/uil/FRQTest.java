package Outlet.uil;

import Outlet.Countdown;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FRQTest {
    public final boolean exists;    // If this FRQ test exists and is being run

    public final String NAME;
    public final String TIME_TEXT;
    public final long TIME;
    public final String STUDENT_PACKET; // Link to the student packet
    public final String JUDGE_PACKET;   // Link to the judge packet

    public static final String SCORE_DIR_ROOT = "/tmp/"; // Where the score_dirs are stores
    public final String SCORE_DIR; // Must end in a "/"

    public static final String TESTCASE_DIR_ROOT = "/opt/UILTestcases/"; // Where the testcase_dirs are stores
    public final String TESTCASE_DIR; // Must end in a "/"

    public final short NUM_PROBLEMS;
    public final short MAX_POINTS;  // Number of points you get if you get the problem first try
    public final short INCORRECT_PENALTY;   // Number of points taken off MAX_POINTS for each incorrect submission
    public final short MIN_POINTS = 0;  // Minimum number of points you can get for solving a problem;

    public final String[] PROBLEM_MAP;  // A list of the problems, formatted like "1. Abril", "2. Brittany"...
    private final String[] DAT_MAP;
    private static ArrayList<ArrayList<Pair>> files = null;

    public Countdown opens; // The time that this opens

    public FRQTest() {
        exists = false; NAME = "";TIME_TEXT="";TIME=0;DAT_MAP= new String[0];STUDENT_PACKET="";JUDGE_PACKET="";
        SCORE_DIR = ""; TESTCASE_DIR = ""; NUM_PROBLEMS = 0; MAX_POINTS = 0; INCORRECT_PENALTY = 0; PROBLEM_MAP = new String[0];
    }
    public FRQTest (String opensString, String sd, String td, short np, short mp, short ip, String[] pm, String na, String timeText, String studentPacket, String judgePacket, long time, String[] datMap) {
        opens = new Countdown(opensString, "");SCORE_DIR = SCORE_DIR_ROOT+sd; TESTCASE_DIR = TESTCASE_DIR_ROOT+td; NUM_PROBLEMS = np; MAX_POINTS = mp; INCORRECT_PENALTY = ip; PROBLEM_MAP = pm;
        NAME = na;exists = true;TIME_TEXT=timeText;TIME=time;DAT_MAP=datMap;STUDENT_PACKET=studentPacket;JUDGE_PACKET=judgePacket;

        try {
            files = new ArrayList();

            for(int i = 1; i <= NUM_PROBLEMS; ++i) {
                System.out.println("--Getting files for probNum " + i + " in path "+TESTCASE_DIR+i+"/");
                files.add(get_files(new File(TESTCASE_DIR + i + "/")));
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }

        try {
            Runtime.getRuntime().exec("mkdir "+ SCORE_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<Pair> get_files(File dir) {
        System.out.println("--Getting Files in directory " + dir.getAbsolutePath() + " which has " + dir.listFiles().length + " files");
        ArrayList<Pair> ret = new ArrayList();
        File[] var2 = dir.listFiles();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            File test = var2[var4];
            System.out.println(">Looking at file " + test.getAbsolutePath());
            File[] var6 = dir.listFiles();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                File ans = var6[var8];
                if (ans.getName().equals(test.getName() + ".a")) {
                    System.out.println(">Found input-output match of " + test.getName() + " and " + ans.getName());
                    ret.add(new Pair(test, ans));
                }
            }
        }

        System.out.println(">Found " + ret.size() + " test cases.");
        return ret;
    }
    public void close(InputStream stdout, InputStream stderr) throws IOException {
        stdout.close();
        stderr.close();
    }
    public int run(String source_file, String exe_file, String sourceDir, int language, short problemNum) throws IOException {
        String compile_cmd = "";
        String run_cmd = "";
        if (language == 0) {
            compile_cmd = "javac " + source_file;
            run_cmd = "cd " + sourceDir + " && java " + exe_file;
        } else if (language == 1) {
            compile_cmd = "";
            run_cmd = "cd " + sourceDir + " && python " + source_file;
        } else if (language == 2) {
            compile_cmd = "cd " + sourceDir + " && g++ -std=c++17 " + source_file + " -o " + exe_file;
            run_cmd = "cd " + sourceDir + " && ./" + exe_file;
        }

        return grade(source_file, compile_cmd, sourceDir, run_cmd, problemNum);
    }

    // 0 is AC
    // 1 is compile error
    // 2 is runtime error
    // 3 is time limit exceeded
    // 4 is wrong answer
    public int grade(String source_file, String compile_cmd, String dir, String run_cmd, short problemNum) throws IOException {
        System.out.printf("Compiling %s\n", source_file);
        System.out.printf("Compiling %s\n", compile_cmd);
        Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", compile_cmd});

        try {
            int ret = p.waitFor();
            if (ret != 0) {
                System.out.println("Program exited with code: " + ret);
                System.out.println("Compilation failure");
                return 1;
            }

            System.out.println("Compilation success");
        } catch (InterruptedException var32) {
            System.out.printf("Compilation failure");
            var32.printStackTrace();
            return 1;
        }

        ArrayList<Pair> problem_dir = files.get(problemNum - 1);
        Iterator var7 = problem_dir.iterator();

        int judge_code;
        do {
            if (!var7.hasNext()) {
                return 0;
            }

            Pair x = (Pair)var7.next();
            File in_file = x.key;
            File ans_file = x.value;
            Runtime.getRuntime().exec(new String[]{"bash", "-c", "ln -s " + in_file.getAbsolutePath() + " " + dir + DAT_MAP[problemNum-1]});
            System.out.println("Test Case " + in_file.getName());
            System.out.println("--Executing command '" + run_cmd + "'");
            Process r = Runtime.getRuntime().exec(new String[]{"bash", "-c", run_cmd});
            InputStream stdout = r.getInputStream();
            InputStream stderr = r.getErrorStream();
            long a = System.currentTimeMillis();
            boolean var16 = false;

            int xcode;
            try {
                if (!r.waitFor(15L, TimeUnit.SECONDS)) {
                    System.out.println("Time limit exceeded");
                    r.destroyForcibly();
                    close(stdout, stderr);
                    return 3;
                }

                xcode = r.waitFor();
            } catch (InterruptedException var31) {
                var31.printStackTrace();
                close(stdout, stderr);
                return 2;
            }

            long b = System.currentTimeMillis();
            ByteArrayOutputStream error_bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            int length;
            while((length = stderr.read(buffer)) != -1) {
                error_bytes.write(buffer, 0, length);
            }

            String errors = error_bytes.toString("UTF-8");
            if (!errors.equals("")) {
                System.out.println("Runtime error");
                System.out.println(errors);
                close(stdout, stderr);
                return 2;
            }

            if (xcode != 0) {
                System.out.println("Runtime error");
                System.out.println("Program exited with code: " + xcode);
                close(stdout, stderr);
                return 2;
            }

            String newline = System.getProperty("line.separator");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder res = new StringBuilder();

            String output;
            for(boolean flag = false; (output = reader.readLine()) != null; flag = true) {
                res.append(flag ? newline : "").append(output);
            }

            output = res.toString();
            System.out.println("output: " + output);
            judge_code = judge(ans_file, output);
            if (judge_code == 0) {
                System.out.println("Correct answer");
            } else if (judge_code == 1) {
                System.out.println("Token mismatch");
            } else {
                System.out.println("EOF mismatch");
            }

            long c = b - a;
            System.out.println("Execution time: " + c + " ms");
            close(stdout, stderr);
        } while(judge_code == 0);

        return 4;
    }

    public int judge(File ans_keyFile, String output) throws IOException {
        Scanner s1 = new Scanner(ans_keyFile);

        String ans_key;
        for(ans_key = ""; s1.hasNextLine(); ans_key = ans_key + s1.nextLine()) {
        }

        ans_key = ans_key.replaceAll("\\s+", "");
        output = output.replaceAll("\\s+", "");
        return ans_key.equals(output) ? 0 : 1;
    }

    public int score(short probNum, byte[] bytes, String fPath, short uid, short tid){
        String extension = "";
        String givenFName = Paths.get(fPath).getFileName().toString();
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(givenFName);
        boolean cntWhitespace = matcher.find();
        if (cntWhitespace) {
            return -10;
        } else {
            int i = givenFName.lastIndexOf(46);
            if (i < 0) {
                return -11;
            } else {
                String givenName = givenFName.substring(0, i);
                if (i > 0) {
                    extension = givenFName.substring(i + 1);
                }

                String directory = "" + uid + "-" + tid + "-" + System.currentTimeMillis() + "/";
                String fileName = SCORE_DIR + directory + givenFName;

                boolean dirMade;
                try {
                    File dir = new File(SCORE_DIR + directory);
                    dirMade = dir.mkdir();
                    if (!dirMade) {
                        System.out.println("ERROR: Cannot create directory, trying another method");
                        Runtime.getRuntime().exec("mkdir "+ SCORE_DIR + directory);
                    }

                    try {
                        OutputStream os = new FileOutputStream(new File(fileName));
                        os.write(bytes);
                        os.close();
                    } catch (Exception var18) {
                        var18.printStackTrace();
                        System.out.println("--Could not write file, trying another method");
                        Runtime.getRuntime().exec("touch " + fileName);
                    }
                } catch (Exception var19) {
                    var19.printStackTrace();
                }

                System.out.println("------------------------------------");
                if (probNum > 0 && probNum <= NUM_PROBLEMS) {
                    dirMade = false;

                    try {
                        int status;
                        if (extension.equals("java")) {
                            System.out.println("Compiling " + fileName + " into " + givenName + " for prob " + probNum);
                            status = run(fileName, givenName, SCORE_DIR + directory, 0, probNum);
                        } else {
                            String exe_file;
                            if (extension.equals("py")) {
                                exe_file = givenName + ".py";
                                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                                status = run(fileName, exe_file, SCORE_DIR + directory, 1, probNum);
                            } else {
                                if (!extension.equals("cpp")) {
                                    return -12;
                                }

                                exe_file = givenName + ".out";
                                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                                status = run(fileName, exe_file, SCORE_DIR + directory, 2, probNum);
                            }
                        }

                        return status;
                    } catch (Exception var17) {
                        var17.printStackTrace();
                        return -1;
                    }
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * Takes in the number of incorrect tries as a negative number and returns the number of points they get
     * @param numTries
     * @return
     */
    public short calcScore(short numTries) {
        return (short)(MAX_POINTS - Math.abs(numTries-1)*INCORRECT_PENALTY);
    }
    public Countdown getTimer(long started) {
        Countdown timer = new Countdown(TIME, started, "frqTimer");
        timer.onDone = "finishFRQ();";
        return timer;
    }
}
