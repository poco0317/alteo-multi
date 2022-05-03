package com.etterna.multi.data.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.etterna.multi.services.PasswordUtil;
import com.etterna.multi.socket.ettpmessage.payload.response.ChartDTO;
import com.etterna.multi.socket.ettpmessage.payload.response.SelectChartResponseMessage;

public class Lobby {

	private String name;
	private String description;
	private String password;
	private String passwordsalt;
	
	private boolean freerate = false;
	private boolean freepick = false;
	private boolean playing = false;
	private boolean forcestart = false;
	private Chart chart;
	private Set<String> commonpacks = new HashSet<>();
	private SelectionMode selectionmode = SelectionMode.CHARTKEY;
	private boolean countdown = false;
	private boolean inCountdown = false;
	private int timer;
	private LobbyState state = LobbyState.SELECTING;
	
	private UserSession owner;
	private Set<UserSession> operators = new HashSet<>();
	private Set<UserSession> players = new HashSet<>();
	
	public boolean canSelect(UserSession user) {
		return freepick || isOwner(user) || isOperator(user); 
	}
	
	public String canStart(UserSession user) {
		String errors = "";
		List<UserSession> busyplayers = players.stream().filter(p -> !p.getState().equals(PlayerState.READY)).collect(Collectors.toList());
		if (busyplayers.size() > 0) {
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
	
	public void enter(UserSession user) {
		user.setLobby(this);
		players.add(user);
		calcCommonPacks();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setPassword(String hashedPassword) {
		String salt = PasswordUtil.getSalt();
		this.passwordsalt = salt;
		this.password = PasswordUtil.hashPassword(hashedPassword, passwordsalt);
	}
	public boolean checkPassword(String password) {
		String hashed = PasswordUtil.hashPassword(password, passwordsalt);
		return hashed.equals(this.password);
	}
	public boolean isFreerate() {
		return freerate;
	}
	public void setFreerate(boolean freerate) {
		this.freerate = freerate;
	}
	public boolean isFreepick() {
		return freepick;
	}
	public void setFreepick(boolean freepick) {
		this.freepick = freepick;
	}
	public boolean isPlaying() {
		return playing;
	}
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
	public Chart getChart() {
		return chart;
	}
	public void setChart(Chart chart) {
		this.chart = chart;
	}
	public SelectionMode getSelectionmode() {
		return selectionmode;
	}
	public void setSelectionmode(SelectionMode selectionmode) {
		this.selectionmode = selectionmode;
	}
	public boolean isCountdown() {
		return countdown;
	}
	public void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}
	public boolean isInCountdown() {
		return inCountdown;
	}
	public void setInCountdown(boolean inCountdown) {
		this.inCountdown = inCountdown;
	}
	public int getTimer() {
		return timer;
	}
	public void setTimer(int timer) {
		this.timer = timer;
	}
	public UserSession getOwner() {
		return owner;
	}
	public void setOwner(UserSession owner) {
		this.owner = owner;
	}

	public Set<String> getCommonpacks() {
		return commonpacks;
	}

	public void setCommonpacks(Set<String> commonpacks) {
		this.commonpacks = commonpacks;
	}

	public Set<UserSession> getOperators() {
		return operators;
	}

	public void setOperators(Set<UserSession> operators) {
		this.operators = operators;
	}

	public Set<UserSession> getPlayers() {
		return players;
	}

	public void setPlayers(Set<UserSession> players) {
		this.players = players;
	}

	public String getPasswordsalt() {
		return passwordsalt;
	}

	public void setPasswordsalt(String passwordsalt) {
		this.passwordsalt = passwordsalt;
	}

	public LobbyState getState() {
		return state;
	}

	public void setState(LobbyState state) {
		this.state = state;
	}

	public String getPassword() {
		return password;
	}

	public boolean isForcestart() {
		return forcestart;
	}

	public void setForcestart(boolean forcestart) {
		this.forcestart = forcestart;
	}
	
	
}
