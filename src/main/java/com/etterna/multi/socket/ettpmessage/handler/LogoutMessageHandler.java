package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.services.SessionService;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;

@Component
public class LogoutMessageHandler extends EttpMessageHandler {

	@Autowired
	private SessionService sessions;
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		sessions.killSession(session);
	}

}