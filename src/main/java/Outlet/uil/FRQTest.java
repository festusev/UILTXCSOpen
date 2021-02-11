package Outlet.uil;

import Outlet.Countdown;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FRQTest {
    public final boolean exists;    // If this FRQ test exists and is being run

    public final String NAME = "Programming";
    public final String TIME_TEXT;
    public final long TIME;
    public final String STUDENT_PACKET; // Link to the student packet
    public final String JUDGE_PACKET;   // Link to the judge packet

    public static final String SCORE_DIR_ROOT = "/tmp/"; // Where the score_dirs are stores
    public String scoreDirPath; // Must end in a "/"
    public File scoreDir;

    public static final String TESTCASE_DIR_ROOT = "/opt/UILTestcases/"; // Where the testcase_dirs are stores
    public String testcaseDirPath; // Must end in a "/"
    public File testcaseDir;

    public final short MAX_POINTS;  // Number of points you get if you get the problem first try
    public final short INCORRECT_PENALTY;   // Number of points taken off MAX_POINTS for each incorrect submission
    public final short MIN_POINTS = 0;  // Minimum number of points you can get for solving a problem;

    public final FRQProblem[] PROBLEM_MAP;  // A list of the problems
    private static ArrayList<Pair<File, File>> files = new ArrayList<>();

    private static Set<PosixFilePermission> FILE_PERMISSIONS = null;
    static {
        FILE_PERMISSIONS = new HashSet<>();
        FILE_PERMISSIONS.add(PosixFilePermission.OWNER_READ);
        FILE_PERMISSIONS.add(PosixFilePermission.OWNER_WRITE);
        FILE_PERMISSIONS.add(PosixFilePermission.OWNER_EXECUTE);

        FILE_PERMISSIONS.add(PosixFilePermission.GROUP_READ);
        FILE_PERMISSIONS.add(PosixFilePermission.GROUP_WRITE);
        FILE_PERMISSIONS.add(PosixFilePermission.GROUP_EXECUTE);

        FILE_PERMISSIONS.add(PosixFilePermission.OTHERS_READ);
        FILE_PERMISSIONS.add(PosixFilePermission.OTHERS_WRITE);
        FILE_PERMISSIONS.add(PosixFilePermission.OTHERS_EXECUTE);
    }

    public Countdown opens; // The time that this opens
    public Countdown closes;

    public FRQTest() {
        exists = false;TIME_TEXT="";TIME=0;STUDENT_PACKET="";JUDGE_PACKET="";
        scoreDirPath = ""; testcaseDirPath = "";MAX_POINTS = 0; INCORRECT_PENALTY = 0; PROBLEM_MAP = new FRQProblem[0];
    }
    public FRQTest(boolean published,String opensString, short mp, short ip, FRQProblem[] pm, String studentPacket, String judgePacket, long time) {
        if(published) {
            opens = new Countdown(opensString, "countdown");MAX_POINTS = mp; INCORRECT_PENALTY = ip; PROBLEM_MAP = pm;
            exists = true;TIME_TEXT=(time/(1000*60)) + " minutes";TIME=time;STUDENT_PACKET=studentPacket;JUDGE_PACKET=judgePacket;
            closes = Countdown.add(opens, TIME, "countdown");
            closes.onDone = "";
        } else {
            MAX_POINTS = mp;
            INCORRECT_PENALTY = ip;
            PROBLEM_MAP = pm;
            exists = true;
            TIME_TEXT = (time / (1000 * 60)) + " minutes";
            TIME = time;
            STUDENT_PACKET = studentPacket;
            JUDGE_PACKET = judgePacket;
            opens = new Countdown(opensString);
            closes = new Countdown(opensString);
        }
    }
    public void initializeFiles() {
        if(testcaseDirPath == null) return;

        try {
            files.clear();

            for(int i = 1; i <= PROBLEM_MAP.length; ++i) {
                // System.out.println("--Getting files for probNum " + i + " in path "+ testcaseDirPath +i+"/");
                files.add(get_files(new File(testcaseDirPath + i + "/")));
            }
        } catch (Exception var1) {
            var1.printStackTrace();
        }
    }

    public void setDirectories(short cid, short uid) {
        System.out.println("Setting directories");
        scoreDirPath = SCORE_DIR_ROOT + cid+"_"+uid+"/";
        testcaseDirPath = TESTCASE_DIR_ROOT + cid+"_"+uid+"/";
        File newScoreDir = new File(scoreDirPath);
        File newTestcaseDir = new File(testcaseDirPath);

        if(scoreDir != null && scoreDir.exists()) {
            scoreDir.renameTo(newScoreDir);
        } else {
            newScoreDir.mkdir();
            scoreDir = newScoreDir;
        }

        if(testcaseDir != null && testcaseDir.exists()) {
            testcaseDir.renameTo(newTestcaseDir);
        } else {
            newTestcaseDir.mkdir();
            testcaseDir = newTestcaseDir;
        }
    }

    public void createProblemDirectories() {
        for(int i=1;i<=PROBLEM_MAP.length;i++) {
            File dir = new File(testcaseDirPath + i);
            dir.mkdir();
        }
    }

    /***
     * Takes in a problemIndices from an old FRQTest. problemIndices is an array of indices, where each member represents
     * the problem's old index. Deletes directories, renames them, and creates new ones.
     * First, we rename all of the directories that are being renamed to  have a 'tmp_' prefix. This prevents issues that
     * would arise if the user swapped the location of two problems.
     *
     * oldNumProblems is the number of problems that used to exist. The difference between this and the length of problemIndices
     * is used to delete problems.
     * @param problemIndices
     */
    public void updateProblemDirectories(short[] problemIndices, int oldNumProblems) {
        boolean[] notDeleted = new boolean[oldNumProblems];  // Used to determine which problem directories should be deleted
        for(int i=0,j=problemIndices.length;i<j;i++) {
            if(problemIndices[i] >= 0) notDeleted[problemIndices[i]] = true; // If the index is less than zero, it is a new problem
        }

        // Delete all of the directories for problems that have been removed
        for(int i=0;i<oldNumProblems;i++) {
            if(!notDeleted[i]) deleteDirectory(new File(testcaseDirPath +(i+1)));
        }

        // Now, rename the problem directories to 'tmp_#' where # is the new problem #. This will also create new directories
        // for new problems.
        File[] dirs = new File[problemIndices.length];
        for(int i=0,j=problemIndices.length;i<j;i++) {
            if(problemIndices[i] < 0) {  // If the index is less than zero, it is a new problem
                File newDir = new File(testcaseDirPath + "tmp_" + (i+1));
                newDir.mkdir();
                dirs[i] = newDir;
            } else {    // In this case, we rename the directory
                File oldDir = new File(testcaseDirPath + (problemIndices[i]+1));
                File destDir = new File(testcaseDirPath + "tmp_"+(i+1));

                oldDir.renameTo(destDir);
                dirs[i] = destDir;
            }
        }

        // Now, rename all of the 'tmp_#' directories to remove the 'tmp_'
        for(int i=0,j=dirs.length;i<j;i++) {
            dirs[i].renameTo(new File(testcaseDirPath +dirs[i].getName().substring(4)));
        }
        initializeFiles();
    }

    public boolean deleteDirectory(File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return dir.delete();
    }

    /* Deletes the testcase directory */
    public void deleteTestcaseDir() {
        deleteDirectory(new File(testcaseDirPath));
    }

    public static Pair get_files(File dir) {
        if(dir == null) {
            System.out.println("Directory is null");
            return null;
        } else if(dir.listFiles() == null) {
            System.out.println("Directory " + dir.getAbsolutePath() + " is empty");
            return null;
        }
        System.out.println("--Getting Files in directory " + dir.getAbsolutePath() + " which has " + dir.listFiles().length + " files");
        File[] files = dir.listFiles();

        for (File file : files) {
            System.out.println(">Looking at file " + file.getAbsolutePath());
            try {
                Files.setPosixFilePermissions(file.toPath(),FILE_PERMISSIONS);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String fileName = file.getName();
            for (File ans : files) {
                if (ans.getName().equals(fileName + ".a")) {  // We found an input-output pair
                    System.out.println(">Found input-output match of " + fileName + " and " + ans.getName());
                    return new Pair(file, ans);
                    /*try {
                        Files.setPosixFilePermissions(ans.toPath(),FILE_PERMISSIONS);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                } else if(fileName.equals(ans.getName() + ".a")) {
                    System.out.println(">Found input-output match of " + ans.getName() + " and " + fileName);
                    return new Pair(ans, file);
                }
            }

            if(fileName.length() > 2 && fileName.substring(fileName.length() - 2).equals(".a")) {
                System.out.println(">Found a lone output file");
                return new Pair(null, file);    // We didn't find an input file, but we found an output file
            } else {
                System.out.println("ERROR: No output file for " + fileName);
                return new Pair(file, null);    // We didn't find an output file, but we found an input file. This is an error.
            }
        }

        System.out.println(">Found no test cases.");
        return null;
    }
    public void close(InputStream stdout, InputStream stderr) throws IOException {
        stdout.close();
        stderr.close();
    }
    public FRQSubmission run(String source_file, String exe_file, String sourceDir, int language, short problemNum) throws IOException {
        String compile_cmd = "";
        String run_cmd = "";
        if (language == 0) {
            compile_cmd = "/usr/bin/javac " + source_file;
            run_cmd = "cd " + sourceDir + " && /usr/bin/java " + exe_file;
        } else if (language == 1) {
            compile_cmd = "";
            run_cmd = "cd " + sourceDir + " && /usr/lib/python3.7/python " + source_file;
        } else if (language == 2) {
            compile_cmd = "cd " + sourceDir + " && /usr/bin/g++ -std=c++17 " + source_file + " -o " + exe_file;
            run_cmd = "cd " + sourceDir + " && ./" + exe_file;
        }

        return grade(source_file, compile_cmd, sourceDir, run_cmd, problemNum);
    }

    // 0 is AC
    // 1 is compile error
    // 2 is runtime error
    // 3 is time limit exceeded
    // 4 is wrong answer
    public FRQSubmission grade(String source_file, String compile_cmd, String dir, String run_cmd, short problemNum) throws IOException {
        System.out.printf("Compiling %s\n", source_file);
        System.out.printf("Compiling %s\n", compile_cmd);
        System.out.println("/bin/chmod -R 777 " + dir);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", compile_cmd, "/bin/chmod -R 777 " + dir});
        long currentTime = System.currentTimeMillis();

        try {
            p.waitFor();

            int ret = p.waitFor();
            if (ret != 0) {
                System.out.println("Program exited with code: " + ret);
                System.out.println("Compilation failure");
                return new FRQSubmission(problemNum, FRQSubmission.Result.COMPILETIME_ERROR, "", "", currentTime);
            }

            System.out.println("Compilation success");
        } catch (InterruptedException var32) {
            System.out.printf("Compilation failure");
            var32.printStackTrace();
            return new FRQSubmission(problemNum, FRQSubmission.Result.COMPILETIME_ERROR, "", "", currentTime);
        }

        System.out.println("Looking for file with probNum="+problemNum);
        Pair<File, File> testcase = files.get(problemNum - 1);

        //do {
        if (testcase == null) {
            System.out.println("Issue loading");
            return new FRQSubmission(problemNum, FRQSubmission.Result.SERVER_ERROR, "", "", currentTime);
        }

        File in_file = testcase.key;
        File ans_file = testcase.value;

        if(ans_file == null) return new FRQSubmission(problemNum, FRQSubmission.Result.SERVER_ERROR, "", "", currentTime);   // There is no answer file. This is a misconfiguration.

        if(in_file != null) {   // Only add in the dat file if there is one
            try {
                Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "/bin/ln -s " + in_file.getAbsolutePath() + " " + dir + PROBLEM_MAP[problemNum - 1].name.toLowerCase() + ".dat"}).waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return new FRQSubmission(problemNum, FRQSubmission.Result.SERVER_ERROR, "", "", currentTime);
            }
            System.out.println("Test Case " + in_file.getName());
        }

        System.out.println("--Executing command '" + run_cmd + "'");
        Process r = null;
        try {
            r = Runtime.getRuntime().exec(new String[]{"bash", "-c", run_cmd});
            r.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new FRQSubmission(problemNum, FRQSubmission.Result.SERVER_ERROR, "", "", currentTime);
        }
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
                return new FRQSubmission(problemNum, FRQSubmission.Result.EXCEEDED_TIME_LIMIT, "", "", currentTime);
            }

            xcode = r.waitFor();
        } catch (InterruptedException var31) {
            var31.printStackTrace();
            close(stdout, stderr);
            return new FRQSubmission(problemNum, FRQSubmission.Result.RUNTIME_ERROR, "", "", currentTime);
        }

        long b = System.currentTimeMillis();
        ByteArrayOutputStream error_bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while((length = stderr.read(buffer)) != -1) {
            error_bytes.write(buffer, 0, length);
        }

        String errors = error_bytes.toString("UTF-8");
        if (!errors.equals("") && !errors.contains("NOTE:")) {
            System.out.println("Runtime error");
            System.out.println(errors);
            close(stdout, stderr);
            return new FRQSubmission(problemNum, FRQSubmission.Result.RUNTIME_ERROR, "", "", currentTime);
        }

        if (xcode != 0) {
            System.out.println("Runtime error");
            System.out.println("Program exited with code: " + xcode);
            close(stdout, stderr);
            return new FRQSubmission(problemNum, FRQSubmission.Result.RUNTIME_ERROR, "", "", currentTime);
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
        FRQSubmission judge_code = judge(ans_file, output);
        judge_code.problemNumber = problemNum;

        long c = b - a;
        System.out.println("Execution time: " + c + " ms");
        close(stdout, stderr);

        if (judge_code.result == FRQSubmission.Result.CORRECT) {
            System.out.println("Correct answer");
        } else if (judge_code.result == FRQSubmission.Result.INCORRECT) {
            System.out.println("Token mismatch");
        }

        return judge_code;
        //} while(judge_code == 0);
    }

    public FRQSubmission judge(File ans_keyFile, String output) throws IOException {
        Scanner s1 = new Scanner(ans_keyFile);

        String ans_key;
        for(ans_key = ""; s1.hasNextLine(); ans_key = ans_key + s1.nextLine()) {}

        // System.out.println("ANSWER KEY:\n" + ans_key);
        ans_key = ans_key.replaceAll("\\s+", "");
        String noWhitespaceOutput = output.replaceAll("\\s+", "");

        return new FRQSubmission((short)0, ans_key.equals(noWhitespaceOutput)?FRQSubmission.Result.CORRECT :FRQSubmission.Result.INCORRECT, "", output, System.currentTimeMillis());
    }

    /***
     * Writes a testcase file to their testcase folder.
     * @param probNum
     * @param bytes
     * @param isInput
     */
    public void setTestcaseFile(int probNum, byte[] bytes, boolean isInput) {
        String path = testcaseDirPath +(probNum+1)+"/1";
        if(!isInput) path+=".a";
        System.out.println("Setting testcase file, probNum="+probNum+", path="+path);
        try {
            File file = new File(path);
            //if(isInput) files.get(probNum-1).get(0).key = file;
            //else files.get(probNum-1).get(0).value = file;
            file.getParentFile().mkdirs();  // If the parents doesn't exist, it will make them
            Files.deleteIfExists(file.toPath());
            Files.createFile(file.toPath(), PosixFilePermissions.asFileAttribute(FILE_PERMISSIONS));
            Files.setPosixFilePermissions(file.toPath(),FILE_PERMISSIONS);

            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.close();
        } catch (Exception var18) {
            var18.printStackTrace();
        }
    }

    public FRQSubmission score(short probNum, byte[] bytes, String fPath, short uid, short tid){
        String extension = "";
        String givenFName = Paths.get(fPath).getFileName().toString();
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(givenFName);
        boolean cntWhitespace = matcher.find();

        long currentTime = System.currentTimeMillis();

        if (cntWhitespace) {
            return new FRQSubmission(probNum,FRQSubmission.Result.EMPTY_FILE, "", "", currentTime);
        } else {
            int i = givenFName.lastIndexOf(46);
            if (i < 0) {
                return new FRQSubmission(probNum,FRQSubmission.Result.UNCLEAR_FILE_TYPE, "", "", currentTime);
            } else {
                String givenName = givenFName.substring(0, i);
                if (i > 0) {
                    extension = givenFName.substring(i + 1);
                }

                String directory = "" + uid + "-" + tid + "-" + System.currentTimeMillis() + "/";
                String fileName = scoreDirPath + directory + givenFName;

                boolean dirMade;
                try {
                    File root = new File(scoreDirPath);
                    if(!root.exists()) root.mkdir();

                    File dir = new File(scoreDirPath + directory);
                    dirMade = dir.mkdir();
                    if (!dirMade) {
                        System.out.println("ERROR: Cannot create directory, trying another method");
                        Runtime.getRuntime().exec("mkdir "+ scoreDirPath + directory).waitFor();
                    }

                    try {
                        File file = new File(fileName);
                        OutputStream os = new FileOutputStream(file);
                        os.write(bytes);
                        os.close();
                        file.setWritable(true, true);
                        file.setExecutable(true, true);
                        file.setReadable(true, true);
                    } catch (Exception var18) {
                        var18.printStackTrace();
                        System.out.println("--Could not write file, trying another method");
                        Runtime.getRuntime().exec("touch " + fileName).waitFor();
                    }
                } catch (Exception var19) {
                    var19.printStackTrace();
                }

                System.out.println("------------------------------------");
                if (probNum > 0 && probNum <= PROBLEM_MAP.length) {
                    dirMade = false;

                    try {
                        FRQSubmission submission;
                        if (extension.equals("java")) {
                            System.out.println("Compiling " + fileName + " into " + givenName + " for prob " + probNum);
                            submission = run(fileName, givenName, scoreDirPath + directory, 0, probNum);
                        } else {
                            String exe_file;
                            if (extension.equals("py")) {
                                exe_file = givenName + ".py";
                                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                                submission = run(fileName, exe_file, scoreDirPath + directory, 1, probNum);
                            } else {
                                if (!extension.equals("cpp")) {
                                    return new FRQSubmission(probNum, FRQSubmission.Result.UNCLEAR_FILE_TYPE, new String(bytes), "", currentTime);
                                }

                                exe_file = givenName + ".out";
                                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                                submission = run(fileName, exe_file, scoreDirPath + directory, 2, probNum);
                            }
                        }

                        submission.input = new String(bytes);
                        return submission;
                    } catch (Exception var17) {
                        var17.printStackTrace();
                        return new FRQSubmission(probNum, FRQSubmission.Result.SERVER_ERROR, new String(bytes), "", currentTime);
                    }
                } else {
                    return new FRQSubmission(probNum, FRQSubmission.Result.SERVER_ERROR, new String(bytes), "", currentTime);
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
        return (short)(Math.abs(MAX_POINTS) - Math.abs(numTries-1)*Math.abs(INCORRECT_PENALTY));
    }
    public Countdown getTimer() {
        Countdown timer = new Countdown(TIME, opens.date.getTime(), "frqTimer");
        timer.onDone = "";
        return timer;
    }
}
