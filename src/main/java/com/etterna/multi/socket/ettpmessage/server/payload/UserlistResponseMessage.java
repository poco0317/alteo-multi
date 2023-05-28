package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserlistResponseMessage {
	
	private List<Player> players;
	
	public UserlistResponseMessage() {}
	public UserlistResponseMessage(Lobby lobby) {
		players = new ArrayList<>();
		for (UserSession user : lobby.getPlayers()) {
			Player p = new Player();
			p.setName(user.getUsername());
			p.setStatus(user.getState().num() + 1);
			p.setReady(user.isReady());
			players.add(p);
		}
	}


	@Getter @Setter
	public class Player {
		private String name;
		private int status;
		private boolean ready;
	}
}
