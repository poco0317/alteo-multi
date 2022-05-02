package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.etterna.multi.data.state.UserSession;

public class LobbyUserlistResponseMessage {
	
	private List<String> users;
	
	public LobbyUserlistResponseMessage() {}
	public LobbyUserlistResponseMessage(Collection<UserSession> sessions) {
		users = sessions.stream().filter(p -> p.getUsername() != null && !p.getUsername().isBlank()).map(p -> p.getUsername()).collect(Collectors.toList());
	}
	public List<String> getUsers() {
		return users;
	}
	public void setUsers(List<String> users) {
		this.users = users;
	}

}
