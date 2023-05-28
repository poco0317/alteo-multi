package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.LoginMessage;

import lombok.Getter;
import lombok.Setter;

@Component
public class LoginMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		LoginMessage msg = readPayload(message, LoginMessage.class);
		
		
		boolean success = multiplayer.createLoginSession(msg.getUser(), msg.getPass(), session);
		LoginResponseMessage response = new LoginResponseMessage();
		
		if (success) {
			response.setLogged(true);
		} else {
			response.setLogged(false);
			response.setMsg("Login failed for some reason.");
		}
		responder.respond(session, "login", response);
	}
	
	@Getter @Setter
	public class LoginResponseMessage {
		private boolean logged;
		private String msg = "";
	}

}
