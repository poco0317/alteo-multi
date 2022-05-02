package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;

public class LeaderboardResponseMessage {
	
	private List<GameplayDTO> scores = new ArrayList<>();;
	
	public LeaderboardResponseMessage() {}
	public LeaderboardResponseMessage(Lobby lobby) {
		for (UserSession u : lobby.getPlayers()) {
			GameplayDTO gg = new GameplayDTO();
			gg.setJdgstr(u.getGameplayJudgments());
			gg.setWife(u.getGameplayWife());
			gg.setUser(u.getUsername());
			scores.add(gg);
		}
	}
	
	public class GameplayDTO {
		private Double wife;
		private String user;
		private String jdgstr;
		public Double getWife() {
			return wife;
		}
		public void setWife(Double wife) {
			this.wife = wife;
		}
		public String getUser() {
			return user;
		}
		public void setUser(String user) {
			this.user = user;
		}
		public String getJdgstr() {
			return jdgstr;
		}
		public void setJdgstr(String jdgstr) {
			this.jdgstr = jdgstr;
		}
	}

}
