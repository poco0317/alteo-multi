package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.List;
import java.util.stream.Collectors;

import com.etterna.multi.data.state.UserSession;

public class LobbyUserlistResponseMessage {
	
	private List<String> users;
	
	public LobbyUserlistResponseMessage() {}
	public LobbyUserlistResponseMessage(List<UserSession> sessions) {
		users = sessions.stream().map(session -> session.getUsername()).sorted().collect(Collectors.toList());
	}
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}

}
