package Outlet;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class WebsocketConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig conf,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        List<String> cookieHeaders = request.getHeaders().get("cookie");
        if (cookieHeaders != null && cookieHeaders.size() > 0) {
            for (String cookieHeader : cookieHeaders) {
                System.out.println("Cookie="+cookieHeader);
                /*Matcher matcher = cookiePattern.matcher(cookieHeader);
                while (matcher.find()) {
                    String cookieKey = matcher.group(1);
                    String cookieValue = matcher.group(2);
                    if (cookieKey.equals("CRASHID")) {
                        sessionId = cookieValue;
                    }
                }*/
            }
        }
        Map<String, Object> properties = conf.getUserProperties();
        properties.put("handshakereq", "hullo");
    }
}
