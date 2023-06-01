package com.etterna.multi.data.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.etterna.multi.data.GameLobby;
import com.etterna.multi.services.EttpResponseMessageService;
import com.etterna.multi.services.LobbyAuditingDispatch;
import com.etterna.multi.services.MultiplayerService;
import com.etterna.multi.services.PasswordUtil;
import com.etterna.multi.services.SessionService;
import com.etterna.multi.socket.ettpmessage.server.payload.ChartDTO;
import com.etterna.multi.socket.ettpmessage.server.payload.DeleteRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.NewRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.PacklistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.SelectChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.UpdateRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.UserlistResponseMessage;

import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
@Getter @Setter
public class Lobby {
	
	@Autowired
	private MultiplayerService multiplayer;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private EttpResponseMessageService messaging;
	
	@Autowired
	private LobbyAuditingDispatch auditDispatch;
	
	private String name;
	private String description;
	private String password;
	private String passwordSalt;
	
	private boolean freerate = false;
	private boolean freepick = false;
	private boolean playing = false;
	private boolean forcestart = false;
	private Set<String> commonpacks = new HashSet<>();
	private SelectionMode selectionmode = SelectionMode.CHARTKEY;
	private boolean countdown = false;
	private boolean inCountdown = false;
	private int timer;
	LobbyState state = LobbyState.SELECTING;
	private Chart chart;
	
	private UserSession owner;
	private Set<UserSession> operators = new HashSet<>();
	private Set<UserSession> players = new HashSet<>();
	private Set<String> bannedUsers = new HashSet<>();
	
	private GameLobby dbGameLobby;
	
	/**
	 * Final step of initial lobby creation
	 */
	public void broadcastCreation() {
		NewRoomResponseMessage response = new NewRoomResponseMessage(this);
		multiplayer.respondAllSessions("newroom", response);
		
		broadcastUserlistUpdate();
	}
	
	public void broadcastDeletion() {
		multiplayer.respondAllSessions("deleteroom", new DeleteRoomResponseMessage(this));
		auditDispatch.roomDeletion(this);
	}
	
	/**
	 * Mostly for updating room state globally
	 */
	public void broadcastUpdate() {
		multiplayer.respondAllSessions("updateroom", new UpdateRoomResponseMessage(this));
	}
	
	public void broadcastUserLeft(UserSession user) {
		broadcastUserlistUpdate();
		messaging.systemNoticeToLobby(this, user.getUsername() + " left.");
	}
	
	public void broadcastUserlistUpdate() {
		UserlistResponseMessage response = new UserlistResponseMessage(this);
		messaging.respondToLobby(this, "userlist", response);
	}
	
	public void broadcastPacklist() {
		PacklistResponseMessage response = new PacklistResponseMessage(this);
		messaging.respondToLobby(this, "packlist", response);
	}
	
	public boolean canSelect(UserSession user) {
		return freepick || isOwner(user) || isOperator(user); 
	}
	
	public String canStart(UserSession user) {
		String errors = "";
		List<UserSession> busyplayers = players.stream().filter(p -> !p.getState().equals(PlayerState.READY)).collect(Collectors.toList());
		if (!forcestart && busyplayers.size() > 0) {
			errors += "Busy Players: ";
			for (UserSession u : busyplayers) {
				errors += u.getUsername() + ", ";
			}
			errors = errors.substring(0, errors.length()-2);
			return errors;
		}
		return errors;
	}
	
	public List<UserSession> playersWhoNeedToReady(UserSession skipuser) {
		return players.stream().filter(u -> !u.isReady() && !u.equals(skipuser)).collect(Collectors.toList());
	}
	
	public String playerListString(List<UserSession> users) {
		if (users.size() == 1) {
			return users.get(0).getUsername();
		}
		if (users.size() == 2) {
			return users.get(0).getUsername() + " and " + users.get(1).getUsername();
		}
		if (users.size() > 2) {
			String most = String.join(", ", users.subList(0, users.size() - 1).toArray(new String[0]));
			most += ", and " + users.get(users.size()-1);
			return most;
		}
		return "";
	}
	
	public String allReady(UserSession selector) {
		String errors = "";
		List<UserSession> nonreadyplayers = playersWhoNeedToReady(selector);
		if (forcestart || nonreadyplayers.size() == 0) {
			return errors;
		}
		if (nonreadyplayers.size() == 1) {
			return nonreadyplayers.get(0).getUsername() + " is not ready.";
		}
		return playerListString(nonreadyplayers) + " are not ready.";
	}
	
	public boolean isOperator(UserSession user) {
		return operators.contains(user);
	}
	
	public boolean isOwner(UserSession user) {
		return owner.equals(user);
	}
	
	public boolean isOperOrOwner(UserSession user) {
		return isOwner(user) || isOperator(user);
	}
	
	/**
	 * Returns true if a new operator was added
	 */
	public boolean toggleOperator(UserSession user) {
		if (isOperator(user)) {
			operators.remove(user);
			return false;
		} else {
			operators.add(user);
			return true;
		}
	}
	
	
	public void calcCommonPacks() {
		Set<String> packs = new HashSet<>();
		if (players.size() > 0) {
			packs.addAll(players.iterator().next().getPacks());
			for (UserSession user : players) {
				packs.retainAll(user.getPacks());
			}
		}
		commonpacks = packs;
	}
	
	public SelectChartResponseMessage serializeChart(Chart chart) {
		if (chart == null) {
			chart = this.chart;
		}
		SelectChartResponseMessage o = new SelectChartResponseMessage();
		if (chart == null) {
			o.setChart(new ChartDTO());
			return o;
		}
		
		ChartDTO dto = getChartDTO(chart);
		o.setChart(dto);
		
		return o;
	}
	
	public ChartDTO getChartDTO(Chart chart) {
		if (chart == null) {
			return null;
		}
		ChartDTO dto = selectionmode.f(chart);
		if (!freerate) {
			dto.setRate(chart.getRate());
		}
		return dto;
	}
	
	public void addBannedUser(String user) {
		bannedUsers.add(user.toLowerCase());
	}
	
	public boolean isBanned(String user) {
		return bannedUsers.contains(user.toLowerCase());
	}
	
	public void unban(String user) {
		bannedUsers.remove(user.toLowerCase());
	}
	
	public void enter(UserSession user) {
		user.setLobby(this);
		players.add(user);
		calcCommonPacks();
	}
	
	public void setPassword(String hashedPassword) {
		String salt = PasswordUtil.getSalt();
		this.passwordSalt = salt;
		this.password = PasswordUtil.hashPassword(hashedPassword, passwordSalt);
	}
	public boolean checkPassword(String password) {
		String hashed = PasswordUtil.hashPassword(password, passwordSalt);
		return hashed.equals(this.password);
	}
	
}
