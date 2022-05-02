package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;

@Component
public class ScoreMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		UserSession user = sessions.getUserSession(session);
		if (user == null || user.getLobby() == null) {
			return;
		}
		
		ScoreResponseMessage score = new ScoreResponseMessage();
		score.setName(user.getUsername());
		score.setScore(message.getPayload());
		for (UserSession u : user.getLobby().getPlayers()) {
			responder.respond(u.getSession(), "score", score);
		}
	}
	
	public class ScoreResponseMessage {
		private String name;
		private Object score;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Object getScore() {
			return score;
		}
		public void setScore(Object score) {
			this.score = score;
		}
	}

}
