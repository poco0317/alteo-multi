package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.PlayerState;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;

@Component
public class CloseEvalMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		UserSession user = sessions.get(session);
		if (user != null) {
			user.setState(PlayerState.READY);
			multiplayer.updateLobbyState(user.getLobby());
		}
	}

}