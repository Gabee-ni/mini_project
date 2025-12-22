package domain;

import javax.servlet.http.HttpSession;
import javax.websocket.server.ServerEndpointConfig;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(
        ServerEndpointConfig sec,
        javax.websocket.server.HandshakeRequest request,
        javax.websocket.HandshakeResponse response
    ) {
        HttpSession session = (HttpSession) request.getHttpSession(); // null 가능
        sec.getUserProperties().put("httpSession", session);
    }
}
