package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.LoginMessage;

@Component
public class LoginMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
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
