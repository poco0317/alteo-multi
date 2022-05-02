package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.CreateRoomMessage;

@Component
public class CreateRoomMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		CreateRoomMessage msg = readPayload(message, CreateRoomMessage.class);
		
		UserSession user = sessions.getUserSession(session);
		if (user == null || user.getUsername() == null) {
			return;
		}
		
		if (msg.getName() == null) {
			responder.chatMessageToLobby(session, ColorUtil.system("Cannot use empty room name."));
			return;
		}
		
		sessions.removeFromLobby(user);
		if (sessions.createLobby(session, msg)) {
			responder.respond(session, "createroom", new CreateRoomResponseMessage(true));
			responder.chatMessageToRoom(session, ColorUtil.system("Created room '"+msg.getName()+"'"), msg.getName());
		} else {
			responder.respond(session, "createroom", new CreateRoomResponseMessage(false));
			responder.chatMessageToLobby(session, ColorUtil.system("Room name already in use."));
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
