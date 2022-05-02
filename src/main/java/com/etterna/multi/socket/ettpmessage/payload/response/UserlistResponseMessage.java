package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;

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
		}
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public class Player {
		private String name;
		private int status;
		private boolean ready;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public boolean isReady() {
			return ready;
		}
		public void setReady(boolean ready) {
			this.ready = ready;
		}
	}
}
