package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;

@Component
public class PingMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		sessions.updateLastPing(session);
		
		if (m_logger.isDebugEnabled()) {
			UserSession user = sessions.get(session);
			if (user != null) {
				m_logger.debug("Ping received {} - {}", session.getId(), user.getUsername());
			} else {
				m_logger.debug("Ping received {}", session.getId());
			}
		}
	}

}
