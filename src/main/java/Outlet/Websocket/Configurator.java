package Outlet.Websocket;

import Outlet.Conn;
import Outlet.User;
import Outlet.UserMap;
import com.google.gson.Gson;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class Configurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig conf,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        List<String> cookieHeaders = request.getHeaders().get("cookie");
        String token;
        User u = null;
        if (cookieHeaders != null && cookieHeaders.size() > 0) {
            for (String cookieHeader : cookieHeaders) {
                System.out.println("Cookie header="+cookieHeader);
                int indexOfStart = cookieHeader.indexOf("token=");
                int indexOfEnd = cookieHeader.indexOf(";", indexOfStart);

                if(indexOfStart < 0) continue;
                else if(indexOfEnd<indexOfStart) token = cookieHeader.substring(indexOfStart + 6);
                else token = cookieHeader.substring(indexOfStart + 6, indexOfEnd);
                u = UserMap.getUserByToken(Conn.getTokenByString(token));
                break;
            }
        }
        Map<String, Object> properties = conf.getUserProperties();
        if(u != null) properties.put("user", u);
    }
}
