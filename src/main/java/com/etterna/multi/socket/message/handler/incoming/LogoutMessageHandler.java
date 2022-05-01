package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.EttpMessageHandler;
import com.etterna.multi.socket.payload.incoming.LogoutMessage;

@Component
public class LogoutMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		LogoutMessage msg = readPayload(message, LogoutMessage.class);
	}

}