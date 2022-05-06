package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.EnterRoomMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.EnterRoomResponseMessage;

@Component
public class EnterRoomMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		EnterRoomMessage msg = readPayload(message, EnterRoomMessage.class);
		UserSession user = sessions.getUserSession(session);
		if (user == null) {
			return;
		}
		
		sessions.removeFromLobby(user);
		Lobby lobby = sessions.getLobby(msg.getName());
		if (lobby != null) {
			if (lobby.getPassword() == null || lobby.getPassword().isBlank()) {
				// no password, come on in
				sessions.enterLobby(user, lobby);
			} else {
				if (lobby.checkPassword(msg.getPass())) {
					// success password
					sessions.enterLobby(user, lobby);
				} else {
					// failed password
					responder.respond(session, "enterroom", new EnterRoomResponseMessage(false));
					responder.systemNoticeToUserInMainLobby(user, ColorUtil.system("Incorrect password."));
				}
			}
		} else {
			if (msg.getDesc() == null) {
				msg.setDesc("");
			}
			sessions.createLobby(session, msg);
			responder.respond(session, "enterroom", new EnterRoomResponseMessage(true));
			
		}
	}

}
