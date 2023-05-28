package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.List;
import java.util.stream.Collectors;

import com.etterna.multi.data.state.Lobby;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RoomDTO {
	
	private String name;
	private String desc;
	private List<String> players;
	private boolean pass;
	private int state;
	
	public RoomDTO() {}
	public RoomDTO(Lobby lobby) {
		name = lobby.getName();
		desc = lobby.getDescription();
		state = lobby.getState().num();
		pass = lobby.getPassword() != null && !lobby.getPassword().isEmpty();
		players = lobby.getPlayers().stream().map(p -> p.getUsername()).collect(Collectors.toList());
	}

}
