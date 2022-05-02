package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;

@Component
public class MissingChartMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		UserSession user = sessions.getUserSession(session);
		if (user == null || user.getUsername() == null || user.getLobby() == null) {
			return;
		}
		
		for (UserSession u : user.getLobby().getPlayers()) {
			responder.chatMessageToRoom(u.getSession(), ColorUtil.system(u.getUsername() + " doesn't have the chart."), user.getLobby().getName());
		}
	}

}