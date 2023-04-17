package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.EnterRoomMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.EnterRoomResponseMessage;

@Component
public class EnterRoomMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		EnterRoomMessage msg = readPayload(message, EnterRoomMessage.class);
		UserSession user = sessions.get(session);
		if (user == null) {
			return;
		}
		
		multiplayer.removeFromLobby(user);
		if (!multiplayer.lobbyExists(msg.getName())) {
			// if a new room must be made
			
			if (msg.getDesc() == null) {
				msg.setDesc("");
			}
			multiplayer.createLobby(session, msg);
			responder.respond(session, "enterroom", new EnterRoomResponseMessage(true));
		} else {
			multiplayer.tryToJoinLobby(user, msg.getName(), msg.getPass());
		}
	}
}
