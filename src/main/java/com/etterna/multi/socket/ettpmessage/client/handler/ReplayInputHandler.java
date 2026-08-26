package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.ReplayInputMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.GameplayReplayUpdateMessage;

@Component
public class ReplayInputHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		ReplayInputMessage msg = readPayload(message, ReplayInputMessage.class);
		
		UserSession user = sessions.get(session);
		if (user == null || user.getUsername() == null || user.getLobby() == null) {
			return;
		}
		
		GameplayReplayUpdateMessage response = new GameplayReplayUpdateMessage();
		response.setSubType("input");
		response.setData(msg);
		response.setUserid(user.getUsername());
		response.setSeq(message.getId());
		
		List<UserSession> recipients = user.getSpectators();
		responder.respondToUsers(recipients, "gameplay_replay_update", response);
	}

}
