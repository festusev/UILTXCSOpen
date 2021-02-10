package Outlet.uil;

import Outlet.Student;
import Outlet.Teacher;
import Outlet.User;
import Outlet.Websocket.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


@ServerEndpoint(value = "/console/sockets/c/{cid}",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class CompetitionSocket {
    private Session session;
    private User user;
    private Competition competition;    // The competition they are viewing

    public static HashMap<Short, CompetitionSocket> connected = new HashMap<>();    // Maps uid to socket
    public static HashMap<Short, ArrayList<CompetitionSocket>> competitions = new HashMap<>(); // Maps cid to a list of CompetitionSockets

    private static Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("cid") String cidS) throws IOException {
        System.out.println("OPEN!!! cid="+cidS);
        try{
            short cid = Short.parseShort(cidS);
            competition = UIL.getCompetition(cid);

            if(!competitions.containsKey(cid) || competitions.get(cid) == null) competitions.put(cid, new ArrayList<>());
            competitions.get(cid).add(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.session = session;

        if(session.getUserProperties().containsKey("user")) {
            System.out.println("Found user");
            User u = (User) session.getUserProperties().get("user");
            user = u;
            if (u != null) connected.put(u.uid, this);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message)
            throws IOException {
        System.out.println("Message="+message);

        String[] data = gson.fromJson(message, String[].class);

        if(!user.teacher && ((Student)user).cids.containsKey(competition.template.cid)) {
            if (data[0].equals("nc")) {   // They are asking a new clarification
                System.out.println("New Clarification");
                Clarification clarification = new Clarification(user.uid, data[1], "", false);

                int index = competition.clarifications.size();  // The index of this clarification in the list
                competition.clarifications.add(clarification);

                CompetitionSocket teacherSocket = connected.get(competition.teacher.uid);
                if(teacherSocket != null) {
                    JsonObject object = new JsonObject();
                    object.addProperty("action", "nc");
                    object.addProperty("name", user.fname + " " + user.lname);
                    object.addProperty("question", clarification.question);
                    object.addProperty("id", index);

                    teacherSocket.send(object.toString());
                } else {
                    System.out.println("Teacher socket is null");
                }

                try {competition.update();} catch(Exception ignored) {}
            }
        } else if(user.teacher && user.uid == competition.teacher.uid) {
            if(data[0].equals("rc")) {   // They are responding to a clarification
                System.out.println("Responding to a Clarification");
                Clarification clarification = competition.clarifications.get(Integer.parseInt(data[1]));

                clarification.responded = true;
                clarification.response = data[2];

                JsonObject object = new JsonObject();
                object.addProperty("action","ac");
                object.addProperty("question", clarification.question);
                object.addProperty("answer", clarification.response);
                String stringified = object.toString();

                // Relay the clarification to all of the people who are connected to this competition and signed up
                ArrayList<CompetitionSocket> sockets = competitions.get(competition.template.cid);
                for(CompetitionSocket socket: sockets) {
                    UserStatus status = UserStatus.getCompeteStatus(socket.user, competition.template.cid);

                    if(status.signedUp) socket.send(stringified);
                }

                try {competition.update();} catch(Exception ignored) {}
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        System.out.println("Closing session");
        if(this.user != null) connected.remove(this.user.uid);
        if(competition != null) competitions.get(competition.template.cid).remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        System.out.println("ERROR!!!");
        throwable.printStackTrace();
    }

    public void send(String msg) throws IOException {
        session.getBasicRemote().sendText(msg);
    }

    public static void broadcast(String message) {
        connected.values().forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}