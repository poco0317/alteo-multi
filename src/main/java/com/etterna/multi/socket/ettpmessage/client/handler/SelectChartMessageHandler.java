package com.etterna.multi.socket.ettpmessage.client.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.client.payload.SelectChartMessage;

@Component
public class SelectChartMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		SelectChartMessage msg = readPayload(message, SelectChartMessage.class);
		
		UserSession user = sessions.get(session);
		if (user == null) {
			return;
		}
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInMainLobby(user, ColorUtil.system("You are not in a room"));
			return;
		}
		if (!user.getLobby().canSelect(user)) {
			responder.systemNoticeToUser(user, ColorUtil.system("You don't have the rights to select a chart!"), user.getLobby().getName());
			return;
		}
		
		multiplayer.selectChart(user, msg);
	}

}
