package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.ArrayList;
import java.util.List;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LeaderboardResponseMessage {
	
	private List<GameplayDTO> scores = new ArrayList<>();
	
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

	@Getter @Setter
	public class GameplayDTO {
		private Double wife;
		private String user;
		private String jdgstr;
	}

}
