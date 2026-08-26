package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.ReplayMissMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.GameplayReplayUpdateMessage;

@Component
public class ReplayMissHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		ReplayMissMessage msg = readPayload(message, ReplayMissMessage.class);
		
		UserSession user = sessions.get(session);
		if (user == null || user.getUsername() == null || user.getLobby() == null) {
			return;
		}
		
		GameplayReplayUpdateMessage response = new GameplayReplayUpdateMessage();
		response.setSubType("miss");
		response.setData(msg);
		response.setUserid(user.getUsername());
		
		List<UserSession> recipients = user.getSpectators();
		responder.respondToUsers(recipients, "gameplay_replay_update", response);
	}
}
