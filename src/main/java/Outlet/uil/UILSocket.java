package Outlet.uil;

import Outlet.Student;
import Outlet.Teacher;
import Outlet.TeacherMap;
import Outlet.User;
import Outlet.Websocket.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


@ServerEndpoint(value = "/console/sockets/uil_list",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class UILSocket {
    private Session session;
    public User user;
    // private Teacher teacher;    // The teacher whose class they are viewing.

    public static HashMap<Short, UILSocket> connected = new HashMap<>();    // Maps uid to socket
    public static HashMap<Short, ArrayList<UILSocket>> classes = new HashMap<>(); // Maps teacher uid to a list of UILSockets

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;

        if(session.getUserProperties().containsKey("user")) {
            // System.out.println("Found user");
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
                        ArrayList<UILSocket> list = new ArrayList<>();
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
        if(user.teacher) {
            JsonArray jsonArray = JsonParser.parseString(message).getAsJsonArray();
            String action = jsonArray.get(0).getAsString();

            if(action.equals("loadJudges")) {
                JsonObject data = new JsonObject();
                data.addProperty("action", "loadJudges");
                data.addProperty("thisUID", user.uid);
                data.add("judges", TeacherMap.json);
                send(data.toString());
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
        System.out.println("Websocket ERROR!!!");
        // throwable.printStackTrace();
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