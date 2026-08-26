package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.LoginMessage;
import com.etterna.util.MultiConstants;

import lombok.Getter;
import lombok.Setter;

@Component
public class LoginMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		LoginMessage msg = readPayload(message, LoginMessage.class);
		
		if (msg.getUser().contains("@")) {
			LoginResponseMessage response = new LoginResponseMessage();
			response.setLogged(false);
			response.setMsg("Dont use an email.");
			UserSession user = sessions.get(session);
			responder.respond(user, "login", response);
			return;
		}
		
		UserSession user = sessions.get(session);
		if (user == null) {
			// nah
			return;
		}
		
		if (user.getEttpcVersion() != MultiConstants.SERVER_VERSION) {
			LoginResponseMessage response = new LoginResponseMessage();
			response.setLogged(false);
			response.setMsg("Your client is too old to log in.");
			responder.respond(user, "login", response);
			return;
		}
		
		
		boolean success = multiplayer.createLoginSession(msg.getUser(), msg.getPass(), session);
		LoginResponseMessage response = new LoginResponseMessage();
		if (success) {
			response.setLogged(true);
		} else {
			response.setLogged(false);
			response.setMsg("Login failed for some reason.");
		}
		responder.respond(user, "login", response);
	}
	
	@Getter @Setter
	public class LoginResponseMessage {
		private boolean logged;
		private String msg = "";
	}

}
