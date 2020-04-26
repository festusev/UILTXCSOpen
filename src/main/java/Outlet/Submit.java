package Outlet;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import static java.nio.charset.StandardCharsets.UTF_8;

/***
 * Shows a graph of the top 7 teams, then below lists the teams in order.
 * Created by Evan Ellis.
 */
@MultipartConfig
public class Submit extends HttpServlet{
    public static final String ZIP_DIR = "/usr/share/jetty9/";
    public static final short NUM_PROBLEMS = 12;
    public static final short MAX_POINTS = 60;
    public static HashMap<Short, String> problemMap;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Dynamic.addPageview();
        if(!Conn.isLoggedIn(request)){
            response.sendRedirect(request.getContextPath());
        }
        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.append("<html>\n" +
                        "<head>\n" +
                        "    <title>Submit - TXCSOpen</title>\n" +
                        "    <meta charset=\"utf-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                        "<link rel=\"icon\" type=\"image/png\" href=\"res/icon.png\">" +
                        "    <link rel=\"stylesheet\" href=\"./css/bootstrap.min.css\">\n" +
                        "    <link href=\"https://fonts.googleapis.com/css2?family=Open+Sans&family=Oswald&family=Work+Sans&display=swap\" rel=\"stylesheet\">" +
                        "    <link rel=\"stylesheet\" href=\"css/style.css\">\n" +
                        "    <link rel=\"stylesheet\" href=\"css/submit.css\">\n" +
                        "    <script src=\"./js/submit.js\"></script>\n" +
                        "</head>\n" +
                        "<body>\n" + Dynamic.loadLoggedInNav());
        if(!Dynamic.competitionOpen()) {
            writer.append("<style>#copyright_notice{position:fixed;}body{overflow:hidden;}</style><div class=\"forbidden\">Submission is Closed Until the Competition Begins.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\""+request.getContextPath()+"/console\">Click Here to Go back.</a></p></div>");
        } else if(Conn.getUser(request).tid >= 0) {    // If the user belongs to a team
            /*writer.append(
                    "   <div class=\"row\" id=\"upperHalf\"><p id=\"submitHeader\">Submit<span>Be sure to gzip your file.</p></div>" +
                            "   <div class=\"row\" id=\"lowerHalf\">" +
                            "       <form id=\"submit\" onsubmit=\"submit(); return false;\" enctype=\"multipart/form-data\">" +
                            "           <label for=\"textfile\">Choose a Result File:</label>" +
                            "           <input type=\"file\" name=\"textfile\" id=\"textfile\" enctype=\"multipart/form-data\"/>" +
                            "           <button id=\"submit\" class=\"chngButton\">Submit</button>" +
                            "        </form>" +
                            "   </div>" +
                            Dynamic.loadLeftFlair() +
                            Dynamic.loadRightFlair() +
                            Dynamic.loadCopyright() +
                            "</body></html>");*/
            String problems = "";
            int numProblems = problemMap.keySet().size();
            for(int i=1; i<=numProblems;i++){
                problems += "  <option value=\""+i+"\">"+problemMap.get(i)+"</option>\n";
            }
            writer.append("<div id=\"centerBox\"><p id=\"submitHeader\">Submit</p><p id=\"inst\">Choose a problem to submit:</p>" +
                    "<form id=\"submit\" onsubmit=\"submit(); return false;\" enctype=\"multipart/form-data\">" +
                    "<select id=\"problem\">\n" +
                    problems +
                    "</select>" +
                    "<input type=\"file\" accept=\".java\" id=\"textfile\"/>" +
                    "<button id=\"submitBtn\" class=\"chngButton\">Submit</button>" +
                    "</form><p id=\"advice\">Confused? Reread the <a href=\"problems\" class=\"link\">problems</a> and review the <a href=\"rules\" class=\"link\">rules</a>.</p></div>");
        } else {    // Otherwise, display a message saying they must be part of a team to submit
            writer.append("<div class=\"forbidden\">You must belong to a team to submit.<p class=\"forbiddenRedirect\"><a class=\"link\" href=\"console\">Join a team here.</a></p></div>");
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        User u = Conn.getUser(request);
        if(u != null && !Conn.isLoggedIn(u.token)){
            writer.write("{\"reload\":\"" +request.getContextPath() + "\"}");
            return;
        }
            if (u == null) { // User isn't logged in
                writer.append("{\"error\":\"User isn't logged in.\"}");
                return;
            } else if (u.team == null || u.tid < 0) {
                writer.append("{\"error\":\"User doesn't belong to a team.\"}");
                return;
            }

            Team t = u.team;
            Part filePart = request.getPart("textfile");
            InputStream fileContent = filePart.getInputStream();

            byte[] bytes = new byte[fileContent.available()];
            fileContent.read(bytes);

            short probNum = Short.parseShort(request.getParameter("probNum"));

            boolean success =  ScoreEngine.score(probNum, bytes);
            t.addRun(probNum, success);
            int status = t.updateTeam();
            if(status != 0) {
                writer.write("{\"error\":\""+Dynamic.SERVER_ERROR+"\"}");
                return;
            }

            // Update the scoreboard
            Scoreboard.generateScoreboard();

            writer.write("{\"success\":\"You gained points!\"}");
            /*File output = new File(ZIP_DIR + t.tid + "." + u.uid + ".7z");
            logger.write("FilePath: " + output.getAbsolutePath());
            output.createNewFile();
            OutputStream oS = new FileOutputStream(output);
            oS.write(bytes);
            oS.close();

            /*String zipPath = output.getAbsolutePath();
            String zipName = output.getName();
            String fname = zipName.substring(0, zipName.lastIndexOf("."));

            logger.write("Zip file is located at " + zipPath + " and the unzipped file name will be " + fname + "\n");

            // First, unzip
            String[] commands = {"/bin/bash", "-c", "/usr/bin/p7zip -d -f -c " + zipPath + " > " + ZIP_DIR + fname};
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(commands);
            LOGGER.info("EXECUTING COMMAND: " + "/usr/bin/p7zip -d -f -c " + zipPath + " > " + ZIP_DIR + fname);

            logger.write("EXECUTING COMMAND: " + "/usr/bin/p7zip -d -f -c " + zipPath + " > " + ZIP_DIR + fname + "\n");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            String s;
            while ((s = stdInput.readLine()) != null) {
                logger.write(s + "\n");
            }

            // Read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                logger.write(s + "\n");
            }

            // Now, score it
            logger.write("EXECUTING COMMAND /usr/bin/ScoreEngine " + ZIP_DIR + fname + "\n");
            proc = rt.exec("/usr/bin/ScoreEngine " + ZIP_DIR + fname);
            LOGGER.info("EXECUTING COMMAND: " + "/usr/bin/ScoreEngine " + ZIP_DIR + fname);

            stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            String score = "";
            while ((s = stdInput.readLine()) != null) {
                logger.write(s);
                score = s;
            }

            // Read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                logger.write(s);
            }

            String[] splitScore = score.split(",");
            int distWon = Integer.parseInt(splitScore[0]);
            double locWon = Double.parseDouble(splitScore[1]);

            t.addRun(distWon, locWon);
            t.updateTeam();

            // Update the scoreboard
            Scoreboard.generateScoreboard();*/
    }
}
class GZIPCompression {
    public static byte[] compress(final String stringToCompress) {
        if (stringToCompress==null || stringToCompress.length() == 0) {
            return null;
        }

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final GZIPOutputStream gzipOutput = new GZIPOutputStream(baos)) {
            gzipOutput.write(stringToCompress.getBytes(UTF_8));
            gzipOutput.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error while compressing!", e);
        }
    }

    public static String decompress(final byte[] compressed) {
        System.out.println("--DECOMPRESSING. LENGTH = " +compressed.length+"--");
        if (compressed == null || compressed.length == 0) {
            return null;
        }
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            String outStr = "";
            String line;
            System.out.println("--DONE UNZIPPING, COPYING OVER RESULT NOW--");
            outStr = bf.lines().collect(Collectors.joining());
            System.out.println("--UNZIPPING WAS SUCCESSFUL--");
            return outStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
class P7zipCompression {
    public static String decompress(final byte[] compressed) throws IOException {
        SevenZFile sevenZFile = new SevenZFile(new SeekableInMemoryByteChannel(compressed));
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        if(entry!=null){
            byte[] content = new byte[(int) entry.getSize()];
            sevenZFile.read(content, 0, content.length);
            return new String(content);
        }
        sevenZFile.close();
        return null;
    }
}