package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.List;
import java.util.stream.Collectors;

import com.etterna.multi.data.state.UserSession;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LobbyUserlistResponseMessage {
	
	private List<String> users;
	
	public LobbyUserlistResponseMessage() {}
	public LobbyUserlistResponseMessage(List<UserSession> sessions) {
		users = sessions.stream().map(session -> session.getUsername()).sorted().collect(Collectors.toList());
	}

}
