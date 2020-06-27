package Outlet.uil;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.*;
import java.io.*;
import static java.lang.System.out;
import javafx.util.Pair;
import java.util.concurrent.TimeUnit;

public class ScoreEngine {
    private static final String SCORE_DIR = "/opt/UILScoring/";
    private static final String TESTCASE_DIR = "/opt/UILTestcases/";
    public static final Short NUM_PROBLEMS = 18;    // The number of programming problems there are
    public static final short MAX_POINTS = 60;
    public static final String[] PROBLEM_MAP = {"1. Abril", "2. Brittany", "3. Emmanuel", "4. Guowei", "5. Ina", "6. Josefa", "7. Kenneth", "8. Magdalena", "9. Noah", "10. Ramiro", "11. Seema", "12. Wojtek", "13. Least Least Common Multiple Sum", "14. Constellations", "15. Power Walking", "16. A Long Piece of String", "17. Really Mean Question", "18. Pattern Finding"};
    private static ArrayList<ArrayList<Pair<File, File> > > files = null;

    public static void initialize() {
        System.out.println("--Initializing Scoring Engine-- ");
        files = new ArrayList<>();
        for (int i = 0; i < NUM_PROBLEMS; ++i) {
            files.add(get_files(new File(TESTCASE_DIR + i + "/")));
        }
    }

    public static int run(String source_file, String exe_file, int language, short problemNum) throws IOException {
        String compile_cmd = "", run_cmd = "";
        if (language == 0) { // java
            compile_cmd = "javac " + source_file;
            run_cmd = "java " + exe_file;
        }
        else if (language == 1) { // python
            compile_cmd = "";
            run_cmd = "python " + source_file;
        }
        else if (language == 2) { // cpp
            compile_cmd = "g++ " + source_file + " -o " + exe_file;
            run_cmd = exe_file;
        }
        return grade(source_file, exe_file, compile_cmd, run_cmd, problemNum);
    }

    // 0 is AC
    // 1 is compile error
    // 2 is runtime error
    // 3 is time limit exceeded
    // 4 is wrong answer
    public static int grade(String source_file, String exe_file, String compile_cmd, String run_cmd, short problemNum) throws IOException {
        out.printf("Compiling %s\n", source_file);
        out.printf("Compiling %s\n", compile_cmd);
        if (compile_cmd.equals("")) {

        }
        else {
            Process p = Runtime.getRuntime().exec(compile_cmd);
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
        }
        ArrayList<Pair<File, File> > problem_dir = files.get(problemNum);
        for (Pair<File, File> x : problem_dir) {
            // out.printf("%s %s\n", x.getKey().getName(), x.getValue().getName());
            File in_file = x.getKey(), ans_file = x.getValue();
            Scanner scan = new Scanner(in_file);

            out.println("------------------------------------");
            out.println("Test Case " + in_file.getName());
            Process r = Runtime.getRuntime().exec(run_cmd);
            InputStream stdout = r.getInputStream();
            InputStream stderr = r.getErrorStream();
            OutputStream stdin = r.getOutputStream();
            PrintWriter stdin_redirect = new PrintWriter(stdin);
            while (scan.hasNextLine()) {
                String s = scan.nextLine();
                stdin_redirect.println(s);
                // out.println(s);
            }
            stdin_redirect.close();

            long a = System.currentTimeMillis();
            // terminate after 5 seconds
            int xcode = 0;
            try {
                if (!r.waitFor(5, TimeUnit.SECONDS)) {
                    out.println("Time limit exceeded");
                    r.destroyForcibly();
                    close(stdin, stdout, stderr);
                    return 3;
                }
                xcode = r.waitFor();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                close(stdin, stdout, stderr);
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
                close(stdin, stdout, stderr);
                return 2;
            }
            if (xcode != 0) {
                out.println("Runtime error");
                out.println("Program exited with code: " + xcode);
                close(stdin, stdout, stderr);
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

            close(stdin, stdout, stderr);

            if (judge_code != 0) return 4;

        }
        return 0;
    }

    public static void close(OutputStream stdin, InputStream stdout, InputStream stderr) throws IOException {
        stdin.close();
        stdout.close();
        stderr.close();
    }

    public static ArrayList<Pair<File, File> > get_files(final File dir) {
        ArrayList<Pair<File, File> > ret = new ArrayList<Pair<File, File> >();
        for (final File test : dir.listFiles()) {
            for (final File ans : dir.listFiles()) {
                if (ans.getName().equals(test.getName() + ".a")) {
                    ret.add(new Pair(test, ans));
                }
            }
        }
        return ret;
    }

    public static int judge(File ans_key, String output) throws IOException {
        Scanner s1 = new Scanner(ans_key);
        Scanner s2 = new Scanner(output);
        while (s1.hasNext() && s2.hasNext()) {
            String t1 = s1.next(), t2 = s2.next();
            if (t1.equals(t2)) continue;
            return 1; // token mismatch
        }
        if (s1.hasNext() || s2.hasNext()) {
            // if (s1.hasNext()) out.println("1");
            // else out.println(s2.next());
            return 2; // eof mismatch
        }
        return 0;
    }
    /**
     * Scores a submission, returning the # districts won, the location
     * @param probNum
     * @return boolean success
     */
    public static int score(short probNum, byte[] bytes, String fPath){
        if(files == null) initialize();

        String extension = "";
        String fileName = SCORE_DIR + Paths.get(fPath).getFileName().toString();

        try {
            OutputStream os = new FileOutputStream(new File(fileName));
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        String name = fileName.substring(0, i);

        System.out.println("--SCORING--");
        if(probNum<=0 || probNum > NUM_PROBLEMS) return -1;

        String exe_file = SCORE_DIR;
        int status = 0;
        try {
            if (extension.equals("java")) {
                exe_file += name + ".class";
                status = run(fileName, exe_file, 0, probNum);
            } else if (extension.equals(".py")) {
                exe_file += name + ".py";
                status = run(fileName, exe_file, 1, probNum);
            } else if (extension.equals("cpp")) {
                exe_file += name + ".exe";
                status = run(fileName, exe_file, 2, probNum);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return status;
    }
}
