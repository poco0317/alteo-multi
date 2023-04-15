package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.GameplayUpdateMessage;

@Component
public class GameplayUpdateMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		GameplayUpdateMessage msg = readPayload(message, GameplayUpdateMessage.class);
		UserSession user = sessions.getUserSession(session);
		if (user != null) {
			user.setGameplayWife(msg.getWife());
			user.setGameplayJudgments(msg.getJdgstr());
			if (user.getLobby() != null) {
				sessions.updateLobbyGameplay(user.getLobby());
			}
		}
	}

}
