package com.etterna.multi.socket.message.handler.incoming;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.amazonaws.util.json.Jackson;
import com.etterna.multi.services.SessionService;
import com.etterna.multi.socket.message.EttpMessage;
import com.etterna.multi.socket.message.IncomingMessageHandler;
import com.etterna.multi.socket.payload.incoming.LoginMessage;

@Component
public class LoginMessageHandler extends IncomingMessageHandler {
	
	@Autowired
	private SessionService sessions;
	
	public static LoginMessage readPayload(EttpMessage msg) {
		return Jackson.fromJsonString(Jackson.toJsonString(msg.getPayload()), LoginMessage.class);
	}

	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		LoginMessage msg = readPayload(message);
		
		
		boolean success = sessions.createLoginSession(msg.getUser(), msg.getPass(), session);
		LoginResponseMessage response = new LoginResponseMessage();
		
		if (success) {
			response.setLogged(true);
		} else {
			response.setLogged(false);
			response.setMsg("Login failed for some reason.");
		}
		respond(session, "login", response);
	}
	
	public class LoginResponseMessage {
		private boolean logged;
		private String msg = "";
		public boolean isLogged() {
			return logged;
		}
		public void setLogged(boolean logged) {
			this.logged = logged;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
	}

}
