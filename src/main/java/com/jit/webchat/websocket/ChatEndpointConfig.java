package com.jit.webchat.websocket;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

// 编写完处理器，ServerEndpointConfig.Configurator 类完成配置：
public class ChatEndpointConfig extends Configurator {
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		// 把 HttpSession 放到 ServerEndpointConfig 中
		HttpSession httpSession = (HttpSession) request.getHttpSession();
		if (httpSession != null) {
			sec.getUserProperties().put("httpSession", httpSession);
		}
	}
}
