package Outlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.*;
import java.io.*;
import static java.lang.System.out;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreEngine {
    private static final String SCORE_DIR = "/tmp/";
    private static final String TESTCASE_DIR = "/opt/UILTestcases/";
    public static final Short NUM_PROBLEMS = 18;    // The number of programming problems there are
    public static final short MAX_POINTS = 60;
    public static final String[] PROBLEM_MAP = {"1. Abril", "2. Brittany", "3. Emmanuel", "4. Guowei", "5. Ina", "6. Josefa", "7. Kenneth", "8. Magdalena", "9. Noah", "10. Ramiro", "11. Seema", "12. Wojtek", "13. Least Least Common Multiple Sum", "14. Constellations", "15. Power Walking", "16. A Long Piece of String", "17. Really Mean Question", "18. Pattern Finding"};
    private static final String[] DAT_MAP = {"abril.dat", "brittany.dat", "emmanuel.dat", "guowei.dat", "ina.dat", "josefa.dat", "kenneth.dat", "magdalena.dat", "noah.dat", "ramiro.dat", "seema.dat", "wojtek.dat", "llcms.dat", "constellations.dat", "powerwalking.dat", "longstring.dat", "rmq.dat", "patternfinding.dat"};
    private static ArrayList<ArrayList<Pair> > files = null;
    private static boolean initialized = false;

    public static void initialize() {
        System.out.println("--Initializing Scoring Engine-- ");
        try {
            files = new ArrayList<>();
            for (int i = 1; i <= NUM_PROBLEMS; ++i) {
                System.out.println("--Getting files for probNum " + i);
                files.add(get_files(new File(TESTCASE_DIR + i + "/")));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        initialized = true;
    }

    public static int run(String source_file, String exe_file, String sourceDir, int language, short problemNum) throws IOException {
        String compile_cmd = "", run_cmd = "";
        if (language == 0) { // java
            compile_cmd = "javac " + source_file;
            run_cmd = "cd " + sourceDir + " && java " + exe_file;
        }
        else if (language == 1) { // python
            compile_cmd = "";
            run_cmd = "cd " + sourceDir + " && python " + source_file;
        }
        else if (language == 2) { // cpp
            compile_cmd = "cd " + sourceDir + " && g++ -std=c++17 " + source_file + " -o " + exe_file;
            run_cmd = "cd " + sourceDir + " && ./" + exe_file;
        }
        return grade(source_file, compile_cmd,sourceDir, run_cmd, problemNum);
    }

    // 0 is AC
    // 1 is compile error
    // 2 is runtime error
    // 3 is time limit exceeded
    // 4 is wrong answer
    public static int grade(String source_file, String compile_cmd, String dir, String run_cmd, short problemNum) throws IOException {
        out.printf("Compiling %s\n", source_file);
        out.printf("Compiling %s\n", compile_cmd);

        Process p = Runtime.getRuntime().exec(new String[]{"bash","-c",compile_cmd});
        try {
            int ret = p.waitFor();
            if (ret != 0) {
                out.println("Program exited with code: " + ret);
                out.println("Compilation failure");
                return 1;
            }
            out.println("Compilation success");
        }
        catch (InterruptedException e) {
            out.printf("Compilation failure");
            e.printStackTrace();
            return 1;
        }
        ArrayList<Pair> problem_dir = files.get(problemNum-1);
        for (Pair x : problem_dir) {
            // out.printf("%s %s\n", x.getKey().getName(), x.getValue().getName());
            File in_file = x.left, ans_file = x.right;

            //Symlink the first dat file to the directory
            Runtime.getRuntime().exec(new String[]{"bash", "-c", "ln -s " + in_file.getAbsolutePath() + " " + dir + DAT_MAP[problemNum-1]});

            out.println("Test Case " + in_file.getName());
            System.out.println("--Executing command '" + run_cmd + "'");
            Process r = Runtime.getRuntime().exec(new String[]{"bash", "-c", run_cmd});
            InputStream stdout = r.getInputStream();
            InputStream stderr = r.getErrorStream();

            long a = System.currentTimeMillis();
            // terminate after 5 seconds
            int xcode = 0;
            try {
                if (!r.waitFor(15, TimeUnit.SECONDS)) {
                    out.println("Time limit exceeded");
                    r.destroyForcibly();
                    close(stdout, stderr);
                    return 3;
                }
                xcode = r.waitFor();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                close(stdout, stderr);
                return 2;
            }
            long b = System.currentTimeMillis();

            // runtime error checking
            ByteArrayOutputStream error_bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stderr.read(buffer)) != -1) {
                error_bytes.write(buffer, 0, length);
            }
            String errors = error_bytes.toString("UTF-8");
            if (!errors.equals("")) {
                out.println("Runtime error");
                out.println(errors);
                close(stdout, stderr);
                return 2;
            }
            if (xcode != 0) {
                out.println("Runtime error");
                out.println("Program exited with code: " + xcode);
                close(stdout, stderr);
                return 2;
            }

            // get output
            String newline = System.getProperty("line.separator");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder res = new StringBuilder();
            boolean flag = false;
            for (String line; (line = reader.readLine()) != null; ) {
                res.append(flag ? newline : "").append(line);
                flag = true;
            }
            String output = res.toString();

            out.println("output: " + output);

            // judging
            int judge_code = judge(ans_file, output);
            if (judge_code == 0) out.println("Correct answer");
            else if (judge_code == 1) out.println("Token mismatch");
            else out.println("EOF mismatch");

            long c = b - a;
            out.println("Execution time: " + c + " ms");

            close(stdout, stderr);

            if (judge_code != 0) return 4;

        }
        return 0;
    }

    public static void close(InputStream stdout, InputStream stderr) throws IOException {
        stdout.close();
        stderr.close();
    }

    public static ArrayList<Pair> get_files(final File dir) {
        System.out.println("--Getting Files in directory " + dir.getAbsolutePath() + " which has " + dir.listFiles().length + " files");
        ArrayList<Pair> ret = new ArrayList<>();
        for (final File test : dir.listFiles()) {
            System.out.println(">Looking at file " + test.getAbsolutePath());
            for (final File ans : dir.listFiles()) {
                if (ans.getName().equals(test.getName() + ".a")) {
                    System.out.println(">Found input-output match of " + test.getName() + " and " + ans.getName());
                    ret.add(new Pair(test, ans));
                }
            }
        }
        System.out.println(">Found " + ret.size() + " test cases.");
        return ret;
    }

    public static int judge(File ans_keyFile, String output) throws IOException {
        Scanner s1 = new Scanner(ans_keyFile);
        String ans_key = "";
        while(s1.hasNextLine()) ans_key += s1.nextLine();

        ans_key = ans_key.replaceAll("\\s+","");
        output = output.replaceAll("\\s+","");

        if(ans_key.equals(output)) return 0;
        else return 1;
    }
    /**
     * Scores a submission, returning the # districts won, the location
     * @param probNum
     * @return boolean success
     */
    public static int score(short probNum, byte[] bytes, String fPath, short uid, short tid){
        if(!initialized) initialize();
        if(!initialized) return -1; // An error occurred;

        String extension = "";
        String givenFName = Paths.get(fPath).getFileName().toString();

        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher = pattern.matcher(givenFName);
        boolean cntWhitespace = matcher.find(); // If the file name contains whitespace
        if(cntWhitespace) return -10;

        int i = givenFName.lastIndexOf('.');
        if(i<0) return -11; // The file has no extension
        String givenName = givenFName.substring(0, i);    // Removes the extension
        if (i > 0) {
            extension = givenFName.substring(i+1);
        }

        String directory = "" + uid + "-" + tid + "-" + System.currentTimeMillis()+"/"; // We make a new dir to run it in
        String fileName = SCORE_DIR + directory + givenFName;

        try {
            // First, create this directory
            File dir = new File(SCORE_DIR + directory);
            boolean dirMade = dir.mkdir();
            if(!dirMade) {
                System.out.println("ERROR: Cannot create directory, trying another method");

                Runtime.getRuntime().exec("mkdir " + SCORE_DIR+directory);
            }

            try {
                OutputStream os = new FileOutputStream(new File(fileName));
                os.write(bytes);
                os.close();
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("--Could not write file, trying another method");
                Runtime.getRuntime().exec("touch " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println("------------------------------------");
        if(probNum<=0 || probNum > NUM_PROBLEMS) return -1;

        String exe_file;
        int status = 0;
        try {
            if (extension.equals("java")) {
                exe_file = givenName;
                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                status = run(fileName, exe_file, SCORE_DIR+directory, 0, probNum);
            } else if (extension.equals("py")) {
                exe_file= givenName + ".py";
                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                status = run(fileName, exe_file, SCORE_DIR+directory, 1, probNum);
            } else if (extension.equals("cpp")) {
                exe_file= givenName + ".out";
                System.out.println("Compiling " + fileName + " into " + exe_file + " for prob " + probNum);
                status = run(fileName, exe_file, SCORE_DIR+directory, 2, probNum);
            } else{
                return -12; // Incorrect extension
            }
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

        return status;
    }
}
class Pair{
    public File left;
    public File right;

    Pair(File l, File r) {
        left = l;
        right = r;
    }
}