package com.etterna.multi.socket.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.amazonaws.util.json.Jackson;

public abstract class IncomingMessageHandler {
	
	protected static final Logger m_logger = LoggerFactory.getLogger(IncomingMessageHandler.class);
	
	public abstract void handle(WebSocketSession session, EttpMessage message);
	
	protected <T> void respond(WebSocketSession session, String messageType, T ettpMessageResponse) {
		try {
			EttpMessageResponse<T> response = new EttpMessageResponse<T>();
			response.setPayload(ettpMessageResponse);
			response.setType(messageType);
			session.sendMessage(new TextMessage(Jackson.toJsonString(response)));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

}
