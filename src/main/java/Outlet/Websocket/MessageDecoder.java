package Outlet.Websocket;

import com.google.gson.Gson;

import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;

public class MessageDecoder implements Decoder.Text<String> {
    private static Gson gson = new Gson();

    @Override
    public String decode(String s) throws DecodeException {
        return s;
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // Custom initialization logic
    }

    @Override
    public void destroy() {
        // Close resources
    }
}
