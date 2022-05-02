package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.StartChartMessage;

@Component
public class StartChartMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		StartChartMessage msg = readPayload(message, StartChartMessage.class);
		
		UserSession user = sessions.getUserSession(session);
		if (user != null && user.getLobby() != null) {
			if (user.getLobby().canSelect(user)) {
				String errors = user.getLobby().canStart(user);
				if (errors == null || errors.isBlank()) {
					sessions.startChart(user, msg);
					sessions.broadcastLobbyUpdate(user.getLobby());
				} else {
					for (UserSession u : user.getLobby().getPlayers()) {
						responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Can't start ("+errors+")"), user.getLobby().getName());
					}
				}
			} else {
				responder.chatMessageToRoom(session, ColorUtil.system("You don't have the rights to start a chart!"), user.getLobby().getName());
			}
		} else if (user.getLobby() == null) {
			responder.chatMessageToUser(session, ColorUtil.system("You are not in a room."));
		}
	}

}