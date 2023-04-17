package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.CreateRoomMessage;

@Component
public class CreateRoomMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		CreateRoomMessage msg = readPayload(message, CreateRoomMessage.class);
		
		UserSession user = sessions.get(session);
		if (user == null || user.getUsername() == null) {
			return;
		}
		
		if (msg.getName() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "Cannot use empty room name.");
			return;
		}
		
		multiplayer.removeFromLobby(user);
		if (multiplayer.createLobby(session, msg)) {
			responder.respond(session, "createroom", new CreateRoomResponseMessage(true));
			multiplayer.systemMessageToGlobalChat(user.getUsername() + " created room '"+msg.getName()+"'");
			responder.systemNoticeToLobby(user.getLobby(), "Welcome to your new room! Use /help to learn about commands.");
		} else {
			responder.respond(session, "createroom", new CreateRoomResponseMessage(false));
			responder.systemNoticeToUserInGlobalChat(user, "Room name already in use.");
		}
	}
	
	public class CreateRoomResponseMessage {
		private boolean created;
		public CreateRoomResponseMessage(boolean c) {
			created = c;
		}
		public CreateRoomResponseMessage() {}
		public boolean getCreated() {
			return created;
		}
		public void setCreated(boolean c) {
			created = c;
		}
	}

}
