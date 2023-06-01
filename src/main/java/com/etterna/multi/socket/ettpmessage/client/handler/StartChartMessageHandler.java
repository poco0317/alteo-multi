package com.etterna.multi.socket.ettpmessage.client.handler;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.StartChartMessage;

@Component
public class StartChartMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) throws IOException {
		StartChartMessage msg = readPayload(message, StartChartMessage.class);
		
		UserSession user = sessions.get(session);
		if (user != null && user.getLobby() != null) {
			if (user.getLobby().canSelect(user)) {
				String errors = user.getLobby().canStart(user);
				if (errors == null || errors.isBlank()) {
					multiplayer.startChart(user, msg);
				} else {
					responder.systemNoticeToLobby(user.getLobby(), "Can't start ("+errors+")");
				}
			} else {
				responder.systemNoticeToUserInRoom(user, "You don't have the rights to start a chart!", user.getLobby().getName());
			}
		} else if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a room.");
		}
	}

}