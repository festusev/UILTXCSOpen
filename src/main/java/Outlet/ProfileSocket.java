package Outlet;

import Outlet.Websocket.*;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;


@ServerEndpoint(value = "/console/sockets/profile",
        configurator = Configurator.class,
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class)
public class ProfileSocket {
    private Session session;
    private User user;

    public static HashMap<Short, ProfileSocket> connected = new HashMap<>();    // Maps uid to socket

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;

        User u = (User) session.getUserProperties().get("user");
        if(u == null) {
            session.close();
            return;
        }
        user = u;
        connected.put(u.uid, this);
    }

    @OnMessage
    public void onMessage(Session session, String message)
            throws IOException {
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        if(user != null) connected.remove(user.uid);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
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