package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.LobbyAuditingDispatch;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.ScoreMessage;

import lombok.Getter;
import lombok.Setter;

@Component
public class ScoreMessageHandler extends EttpMessageHandler {
	
	@Autowired
	private LobbyAuditingDispatch auditDispatch;
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		UserSession user = sessions.get(session);
		if (user == null || user.getLobby() == null) {
			return;
		}
		
		ScoreMessage msg = readPayload(message, ScoreMessage.class);
		ScoreResponseMessage score = new ScoreResponseMessage();
		score.setName(user.getUsername());
		score.setScore(message.getPayload());
		
		m_logger.info("Processing score from {} - {} {} - {} - {} - {}", user.getUsername(), msg.getChartkey(), msg.getScorekey(), msg.getSsr_norm(), msg.getMods(), msg.getRate());
		
		responder.respondToLobby(user.getLobby(), "score", score);
		String pb = msg.getTopscore() == 1 ? " - a new PB!" : "";
		responder.systemNoticeToLobby(user.getLobby(), 
				user.getUsername() + " just set a " 
				+ String.format("%5.4f%%", msg.getSsr_norm() * 100.0) 
				+ " on " + user.getLobby().getChart().getTitle() + " (" +user.getLobby().getChart().getDifficulty()+ ") - " 
				+ String.format("%3.2fx", msg.getRate())
				+ pb);
		
		auditDispatch.roomScore(user, msg);
	}
	
	@Getter @Setter
	public class ScoreResponseMessage {
		private String name;
		private Object score;
	}

}
