package Outlet.uil;

import Outlet.Teacher;
import Outlet.User;
import Outlet.Websocket.*;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


@ServerEndpoint(value = "/compsocket/{cid}",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class CompetitionSocket {
    private Session session;
    private User user;
    private Competition competition;    // The competition they are viewing

    public static HashMap<Short, CompetitionSocket> connected = new HashMap<>();    // Maps uid to socket
    public static HashMap<Short, ArrayList<CompetitionSocket>> competitions = new HashMap<>(); // Maps cid to a map that maps uids to CompetitionSockets

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
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        if(this.user != null) connected.remove(this.user.uid);
        competitions.get(competition.template.cid).remove(this);
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