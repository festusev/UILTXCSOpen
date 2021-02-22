package Outlet.uil;

import Outlet.*;
import Outlet.Websocket.*;
import com.google.gson.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


@ServerEndpoint(value = "/console/sockets/class",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class ClassSocket {
    private Session session;
    public User user;
    // private Teacher teacher;    // The teacher whose class they are viewing.

    public static HashMap<Short, ClassSocket> connected = new HashMap<>();    // Maps uid to socket
    public static HashMap<Short, ArrayList<ClassSocket>> classes = new HashMap<>(); // Maps teacher uid to a list of ClassSockets
    private static Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;

        if(session.getUserProperties().containsKey("user")) {
            System.out.println("Found user");
            user = (User) session.getUserProperties().get("user");
            if (user != null) {
                connected.put(user.uid, this);
                short tid;
                if(user.teacher) tid = user.uid;
                else tid = ((Student)user).teacherId;

                if(tid >= 0) {
                    if(classes.containsKey(tid)) {
                        classes.get(tid).add(this);
                    } else {
                        ArrayList<ClassSocket> list = new ArrayList<>();
                        list.add(this);
                        classes.put(tid, list);
                    }
                }
            }
            else session.close();
        } else {
            session.close();
        }
    }

    @OnMessage
    public void onMessage(Session session, String message)
            throws IOException {
        System.out.println("Message="+message);

        if(user.teacher) {
            JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
            String action = jsonObject.get("action").getAsString();
            boolean save = action.equals("save");
            boolean create = action.equals("create");
            if (save || create) {   // They are saving or creating a team
                System.out.println("Action: " + action);

                String tname = jsonObject.get("name").getAsString();
                JsonArray nonAltUIDsJson = jsonObject.get("nonAlts").getAsJsonArray();
                ArrayList<Student> nonAltStudents = new ArrayList<>();
                for (JsonElement uid : nonAltUIDsJson) {
                    nonAltStudents.add(StudentMap.getByUID(uid.getAsShort()));
                }

                Student alt = null;
                if (!jsonObject.get("alt").isJsonNull()) alt = StudentMap.getByUID(jsonObject.get("alt").getAsShort());

                Team team;
                if(save) {
                    short tid = jsonObject.get("tid").getAsShort();
                    team = Team.teams.get(user.uid).get(tid);
                    team.name = tname;
                    team.nonAltStudents = nonAltStudents;
                    team.alternate = alt;
                } else {
                    team = new Team((Teacher)user, tname, nonAltStudents, alt);
                }

                try {
                    team.update(create);
                    if(create) {    // In this case, we must send back the team's tid
                        team.setTID(team.tid);  // Add this team to the hashmap

                        JsonObject response = new JsonObject();
                        response.addProperty("action", "setTID");
                        response.addProperty("tid", team.tid);
                        response.addProperty("reference", jsonObject.get("reference").getAsShort());    //  Used so that the javascript can find the object again
                        send(response.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else if(action.equals("delete")) {
                short tid = jsonObject.get("tid").getAsShort();
                HashMap<Short, Team> teamMap = Team.teams.get(user.uid);
                if(teamMap != null) {
                    Team team = teamMap.get(tid);
                    if(team.tid == tid) {   // We do this to make sure that the wrong team isn't deleted
                        try {
                            team.delete();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        if(this.user != null) connected.remove(this.user.uid);
        short tid;
        if(user.teacher) tid = user.uid;
        else tid = ((Student)user).teacherId;
        if(tid >= 0) classes.get(tid).remove(this);
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