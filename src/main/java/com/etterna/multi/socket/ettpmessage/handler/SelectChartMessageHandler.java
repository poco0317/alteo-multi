package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.SelectChartMessage;

@Component
public class SelectChartMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		SelectChartMessage msg = readPayload(message, SelectChartMessage.class);
		
		UserSession user = sessions.getUserSession(session);
		if (user == null) {
			responder.chatMessageToUser(session, ColorUtil.system("Internal server error - Your connection does not map to a user"));
			return;
		}
		if (user.getLobby() == null) {
			responder.chatMessageToUser(session, ColorUtil.system("You are not in a room"));
			return;
		}
		if (!user.getLobby().canSelect(user)) {
			responder.chatMessageToRoom(session, ColorUtil.system("You don't have the rights to select a chart!"), user.getLobby().getName());
			return;
		}
		
		sessions.selectChart(user, msg);
	}

}
