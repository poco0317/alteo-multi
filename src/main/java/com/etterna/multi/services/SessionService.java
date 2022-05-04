package com.etterna.multi.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.Chart;
import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.LobbyState;
import com.etterna.multi.data.state.PlayerState;
import com.etterna.multi.data.state.SelectionMode;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.payload.CreateRoomMessage;
import com.etterna.multi.socket.ettpmessage.payload.EnterRoomMessage;
import com.etterna.multi.socket.ettpmessage.payload.HelloMessage;
import com.etterna.multi.socket.ettpmessage.payload.SelectChartMessage;
import com.etterna.multi.socket.ettpmessage.payload.StartChartMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.ChartDTO;
import com.etterna.multi.socket.ettpmessage.payload.response.DeleteRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.EnterRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.LeaderboardResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.LobbyUserlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.LobbyUserlistUpdateResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.NewRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.PacklistResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.RoomDTO;
import com.etterna.multi.socket.ettpmessage.payload.response.RoomlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.SelectChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.StartChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.UpdateRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.payload.response.UserlistResponseMessage;

@Service
public class SessionService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(SessionService.class);

	@Autowired
	private UserLoginService loginService;
	
	@Autowired
	private ResponseService responder;
	
	// session ids to usersessions
	// usersessions dont necessarily have any user logged in
	private ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Object> logins = new ConcurrentHashMap<>();
	private static final Object NOTHING = new Object();
	
	// room names to lobbies
	private ConcurrentHashMap<String, Lobby> rooms = new ConcurrentHashMap<>();
	
	// countdown threads
	private ConcurrentHashMap<String, Thread> countdowns = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 1000L * 10L)
	private void maintainSessions() {
		if (logins.size() == 0) {
			m_logger.debug("Skipping session maintenance - no sessions");
			return;
		}
		m_logger.debug("Running session maintenance - {} sessions - {} logins", sessions.size(), logins.size());
		
		// prune forgotten dead sessions and logins
		Iterator<Entry<String, UserSession>> it = sessions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UserSession> e = it.next();
			UserSession user = e.getValue();
			String username = user.getUsername();
			if (user.getSession() != null && !user.getSession().isOpen()) {
				if (logins.containsKey(username)) {
					logins.remove(username);
				}
				it.remove();
			}
		}
		
		// prune orphaned logins
		Iterator<Entry<String, Object>> loginit = logins.entrySet().iterator();
		while (loginit.hasNext()) {
			Entry<String, Object> e = loginit.next();
			// find out if any logins represent orphaned sessions
			if (sessions.values().stream().filter(us -> us.getUsername() != null && us.getUsername().equals(e.getKey())).count() == 0) {
				loginit.remove();
			}
		}
		
		m_logger.debug("Session maintenance complete - {} sessions - {} logins", sessions.size(), logins.size());
	}
	
	@Scheduled(fixedDelay = 1000L * 10L)
	private void maintainLobbies() {
		if (rooms.size() == 0) {
			m_logger.debug("Skipping lobby maintenance - no lobbies");
			return;
		}
		m_logger.debug("Running lobby maintenance - {} lobbies", rooms.size());
		
		// remove empty lobbies
		Iterator<Entry<String, Lobby>> it = rooms.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Lobby> e = it.next();
			Lobby l = e.getValue();
			if (l.getPlayers().size() == 0) {
				it.remove();
			}
		}
		
		// remove dead lobby references
		for (UserSession user : sessions.values()) {
			if (user.getLobby() != null && getLobby(user.getLobby().getName()) == null) {
				user.setLobby(null);
			}
		}
		
		m_logger.debug("Lobby maintenance complete - {} lobbies", rooms.size());
	}
	
	/**
	 * Get a user session from the connection
	 */
	public UserSession getUserSession(WebSocketSession session) {
		return sessions.get(session.getId());
	}
	
	/**
	 * Get the lobby by name
	 */
	public Lobby getLobby(String name) {
		return rooms.get(name.toLowerCase());
	}
	
	/**
	 * Get the lobby a user connection is in
	 */
	public Lobby getLobby(WebSocketSession session) {
		return getLobby(getUserSession(session));
	}
	
	/**
	 * Get the lobby from a user session
	 */
	public Lobby getLobby(UserSession user) {
		if (user != null) {
			return user.getLobby();
		}
		return null;
	}
	
	public void pingSession(String username) {
		UserSession user = sessions.get(username);
		if (user != null) {
			user.setLastPing(System.currentTimeMillis());
		}
	}
	
	public void killSession(WebSocketSession session) {
		String id = session.getId();
		if (session.isOpen()) {
			try {
				session.close();
			} catch (IOException e) {
				m_logger.error(e.getMessage(), e);
			}
		}
		if (sessions.containsKey(id)) {
			UserSession user = sessions.get(id);
			if (user.getUsername() != null) {
				logins.remove(user.getUsername());
				broadcastConnectedUserLobbylistRemoval(user);
				removeFromLobby(user);
			}
			sessions.remove(id);
			m_logger.info("Killed ws session - {}", id);
		}		
	}
	
	public void killSessionByUsername(String username) {
		Iterator<Entry<String, UserSession>> it = sessions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UserSession> e = it.next();
			UserSession u = e.getValue();
			if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
				if (u.getSession() != null && u.getSession().isOpen()) {
					try {
						u.getSession().close();
					} catch (IOException e1) {
						m_logger.error(e1.getMessage(), e1);
					}
				}
				broadcastConnectedUserLobbylistRemoval(u);
				removeFromLobby(u);
				it.remove();
				m_logger.info("Killed ws session by user - {}", username);
			}
		}
	}
	
	public void registerGeneralSession(WebSocketSession session) {
		UserSession user = new UserSession();
		user.setSession(session);
		sessions.put(session.getId(), user);
	}
	
	/**
	 * Send a message to the lobby tab of every connected user
	 */
	public void messageAllSessions(UserSession sender, String message) {
		if (sender == null || sender.getUsername() == null) {
			m_logger.warn("Attempted to send message to everyone using empty name - Message: {}", message);
			return;
		}
		String senderName = ColorUtil.colorize(sender.getUsername(), ColorUtil.COLOR_PLAYER);
		String messageToSend = senderName + ": " + message;
		for (UserSession user : sessions.values()) {
			responder.chatMessageToLobby(user.getSession(), messageToSend);
		}
	}
	
	/**
	 * Send data to all users
	 */
	public <T> void respondAllSessions(String messageType, T ettpMessageResponse) {
		for (UserSession user : sessions.values()) {
			responder.respond(user.getSession(), messageType, ettpMessageResponse);
		}
	}
	
	/**
	 * Privately send a message to a sender and a receiver
	 */
	public void privateMessage(UserSession sender, String recipientName, String message) {
		if (sender == null || recipientName == null || message == null || sender.getUsername() == null) {
			return;
		}
		if (logins.containsKey(recipientName)) {
			UserSession recipient = null;
			for (UserSession u : sessions.values()) {
				if (u.getUsername() != null && u.getUsername().equals(recipientName)) {
					recipient = u;
					break;
				}
			}
			String colorizedMsg = ColorUtil.colorize(sender.getUsername(), ColorUtil.COLOR_PLAYER) + ": " + message;
			responder.chatMessageToRoom(sender.getSession(), colorizedMsg, recipientName);
			responder.chatMessageToRoom(recipient.getSession(), colorizedMsg, sender.getUsername()); 
		} else {
			responder.chatMessageToUser(sender.getSession(), ColorUtil.system("Could not find user '"+recipientName+"'"));
		}
	}
	
	public boolean createLoginSession(String username, String password, WebSocketSession session) {
		boolean allowed = loginService.login(username, password);
		
		if (!allowed) {
			// user gave a wrong password or there was some other error
			return false;
		}
		
		if (logins.containsKey(username)) {
			// user is already logged in
			// kill previous session
			killSessionByUsername(username);
		}
		
		UserSession user = sessions.get(session.getId());
		if (user != null) {
			// login
			user.setLastPing(System.currentTimeMillis());
			user.setUsername(username);
			logins.put(username, NOTHING);
			refreshConnectedUserList(user);
			broadcastConnectedUserLobbylistAddition(user);
			sendAllRooms(user);
		} else {
			// user is not connected?
			m_logger.error("Session could not be found for user {} - session {}", username, session.getId());
		}
		return true;
	}
	
	/**
	 * Shadow for CreateRoomMessage
	 */
	public boolean createLobby(WebSocketSession session, EnterRoomMessage msg) {
		CreateRoomMessage amsg = new CreateRoomMessage();
		amsg.setDesc(msg.getDesc());
		amsg.setName(msg.getName());
		amsg.setPass(msg.getPass());
		return createLobby(session, amsg);
	}
	
	public boolean createLobby(WebSocketSession session, CreateRoomMessage msg) {
		String name = msg.getName();
		Lobby lobby = getLobby(name);
		if (lobby != null) {
			return false;
		}
		
		UserSession user = getUserSession(session);
		if (user == null) {
			return false;
		}
		
		lobby = new Lobby();
		lobby.setOwner(user);
		lobby.getPlayers().add(user);
		lobby.setSelectionmode(SelectionMode.CHARTKEY);
		lobby.setName(msg.getName());
		lobby.setDescription(msg.getDesc());
		if (msg.getPass() != null && !msg.getPass().isBlank()) {
			lobby.setPassword(msg.getPass());
		}
		lobby.calcCommonPacks();
		
		user.setLobby(lobby);
		user.setState(PlayerState.READY);
		user.setReady(false);
		
;		rooms.put(name.toLowerCase(), lobby);
		broadcastRoomCreation(lobby);
		refreshLobbyUserList(lobby);
		
		return true;
	}
	
	public void enterLobby(UserSession user, Lobby lobby) {
		lobby.enter(user);
		responder.respond(user.getSession(), "enterroom", new EnterRoomResponseMessage(true));
		for (UserSession u : lobby.getPlayers()) {
			responder.chatMessageToRoom(u.getSession(), ColorUtil.system(user.getUsername()+" joined."), lobby.getName());
		}
		user.setState(PlayerState.READY);
		if (lobby.getChart() != null) {
			responder.respond(user.getSession(), "selectchart", null);
		}
		respondAllSessions("updateroom", new UpdateRoomResponseMessage(lobby));
		refreshLobbyUserList(lobby);
		refreshPackList(lobby);
	}
	
	public void removeFromLobby(UserSession user) {
		Lobby lobby = getLobby(user);
		if (lobby != null) {
			// user no longer in this lobby
			user.setLobby(null);
			
			// remove references to this user from the lobby
			lobby.getPlayers().remove(user);
			lobby.getOperators().remove(user);
			if (lobby.getOwner().equals(user) && lobby.getPlayers().size() > 0) {
				lobby.setOwner(lobby.getPlayers().iterator().next());
			}
			
			// remove empty lobby
			if (lobby.getPlayers().isEmpty()) {
				rooms.remove(lobby.getName());
				respondAllSessions("deleteroom", new DeleteRoomResponseMessage(lobby));
			} else {
				// otherwise make sure common packs is updated
				lobby.calcCommonPacks();
				refreshLobbyUserList(lobby);
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system(user.getUsername() + " left."), lobby.getName());
				}
			}
			responder.chatMessageToLobby(user.getSession(), ColorUtil.system("Left room '"+lobby.getName()+"'"));
		}
	}
	
	public void updateLobbyState(Lobby lobby) {
		if (lobby == null) {
			return;
		}
		
		LobbyState before = lobby.getState();
		lobby.setState(LobbyState.SELECTING);
		lobby.getPlayers().forEach(p -> {
			if (!p.getState().equals(PlayerState.READY)) {
				lobby.setState(LobbyState.INGAME);
			}
		});
		refreshLobbyUserList(lobby);
		if (lobby.getState().equals(LobbyState.SELECTING) && lobby.isPlaying()) {
			lobby.setPlaying(false);
			lobby.setChart(null);
		}
		
		if (!lobby.getState().equals(before)) {
			broadcastLobbyUpdate(lobby);
		}
	}
	
	public void broadcastLobbyUpdate(Lobby lobby) {
		if (lobby != null) {
			respondAllSessions("updateroom", new UpdateRoomResponseMessage(lobby));
		}
	}
	
	/**
	 * Send the entire connected logged in userlist to a specific user
	 */
	public void refreshConnectedUserList(UserSession recipient) {
		LobbyUserlistResponseMessage response = new LobbyUserlistResponseMessage(sessions.values());
		responder.respond(recipient.getSession(), "lobbyuserlist", response);
	}
	
	public void broadcastConnectedUserLobbylistRemoval(UserSession leaver) {
		LobbyUserlistUpdateResponseMessage response = new LobbyUserlistUpdateResponseMessage();
		response.setOff(leaver.getUsername());
		respondAllSessions("lobbyuserlistupdate", response);
	}
	
	public void broadcastConnectedUserLobbylistAddition(UserSession joiner) {
		LobbyUserlistUpdateResponseMessage response = new LobbyUserlistUpdateResponseMessage();
		response.setOn(joiner.getUsername());
		respondAllSessions("lobbyuserlistupdate", response);
	}
	
	public void broadcastRoomCreation(Lobby lobby) {
		NewRoomResponseMessage response = new NewRoomResponseMessage(lobby);
		respondAllSessions("newroom", response);
	}
	
	public void broadcastAllRooms() {
		List<RoomDTO> roomdtos = rooms.values().stream().map(p -> new RoomDTO(p)).collect(Collectors.toList());
		respondAllSessions("roomlist", new RoomlistResponseMessage(roomdtos));
	}
	
	public void sendAllRooms(UserSession user) {
		List<RoomDTO> roomdtos = rooms.values().stream().map(p -> new RoomDTO(p)).collect(Collectors.toList());
		responder.respond(user.getSession(), "roomlist", new RoomlistResponseMessage(roomdtos));
	}
	
	/**
	 * For rooms/lobbies only
	 */
	public void refreshLobbyUserList(Lobby lobby) {
		if (lobby == null) {
			return;
		}
		UserlistResponseMessage response = new UserlistResponseMessage(lobby);
		for (UserSession user : lobby.getPlayers()) {
			responder.respond(user.getSession(), "userlist", response);
		}
	}
	
	public void refreshPackList(Lobby lobby) {
		if (lobby == null) {
			return;
		}
		PacklistResponseMessage response = new PacklistResponseMessage(lobby);
		for (UserSession user : lobby.getPlayers()) {
			responder.respond(user.getSession(), "packlist", response);
		}
	}
	
	public void selectChart(UserSession user, StartChartMessage msg) {
		selectChart(user, new SelectChartMessage(msg));
	}
	
	public void selectChart(UserSession user, SelectChartMessage msg) {
		Chart chart = new Chart(msg);
		chart.setPickedBy(user.getUsername());
		Lobby lobby = user.getLobby();
		if (lobby != null) {
			lobby.setChart(chart);
		} else {
			return;
		}

		SelectChartResponseMessage response = user.getLobby().serializeChart(chart);
		Double rate = response.getChart().getRate();
		String ratestr = rate != null ? String.format("%.2f", rate / 1000) : "";
		String chatmsg = String.format("%s selected %s (%s) %s %s",
				user.getUsername(),
				chart.getTitle(),
				chart.getDifficulty(),
				ratestr,
				msg.getPack() != null ? msg.getPack() : "");
		for (UserSession u : user.getLobby().getPlayers()) {
			responder.respond(u.getSession(), "selectchart", response);
			responder.chatMessageToRoom(u.getSession(), ColorUtil.system(chatmsg), user.getLobby().getName());
		}
	}
	
	public void startChart(UserSession user, StartChartMessage msg) {
		Lobby lobby = user.getLobby();
		if (lobby.isCountdown()) {
			String errors = lobby.allReady(user);
			if (errors != null && !errors.isBlank()) {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system(errors), lobby.getName());
				}
				return;
			}
			lobby.getPlayers().forEach(p -> p.setReady(false));
			lobby.setForcestart(false);
			startCountdown(user, msg);
		} else {
			startChartInternal(user, msg);
		}
	}
	
	private void startChartInternal(UserSession user, StartChartMessage msg) {
		Lobby lobby = user.getLobby();
		if (lobby == null) {
			return;
		}
		Chart chart = new Chart(msg);
		
		ChartDTO newch = lobby.getChartDTO(chart);
		ChartDTO oldch = lobby.getChartDTO(lobby.getChart());
		if (lobby.getChart() == null || !newch.equals(oldch)) {
			selectChart(user, msg);
			return;
		}
		
		String errors = lobby.allReady(user);
		if (errors != null && !errors.isBlank()) {
			for (UserSession u : lobby.getPlayers()) {
				responder.chatMessageToRoom(u.getSession(), ColorUtil.system(errors), lobby.getName());
			}
			return;
		}
		lobby.getPlayers().forEach(p -> p.setReady(false));
		lobby.setForcestart(false);
		lobby.setChart(chart);
		lobby.setState(LobbyState.INGAME);
		lobby.setPlaying(true);
		StartChartResponseMessage response = new StartChartResponseMessage(newch);
		for (UserSession u : lobby.getPlayers()) {
			responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Starting "+chart.getTitle()), lobby.getName());
			responder.respond(u.getSession(), "startchart", response);
		}
	}
	
	public void startCountdown(UserSession user, StartChartMessage msg) {
		Lobby lobby = user.getLobby();
		if (lobby == null || lobby.isInCountdown()) {
			return;
		}
		lobby.setInCountdown(true);
		Runnable work = new Runnable() {
			public void run() {
				final String nm = lobby.getName();
				int left = lobby.getTimer();
				while (left > 0) {
					for (UserSession u : lobby.getPlayers()) {
						if (u.getSession().isOpen()) {
							responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Starting in "+left+" seconds."), lobby.getName());
						}
					}
					left--;
					try {
						Thread.sleep(1000L);
					} catch (Exception e) {}
				}
				for (UserSession u : lobby.getPlayers()) {
					if (u.getSession().isOpen()) {
						responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Starting song."), lobby.getName());
					}
				}
				lobby.setInCountdown(false);
				countdowns.remove(nm);
				startChartInternal(user, msg);
			}
		};
		Thread t = new Thread(work);
		countdowns.put(lobby.getName(), t);
		t.start();
	}
	
	public void stopCountdown(Lobby lobby) {
		if (lobby == null || !countdowns.containsKey(lobby.getName())) {
			return;
		}
		
		try {
			countdowns.get(lobby.getName()).interrupt();
		} catch (Exception e) {}
		countdowns.remove(lobby.getName());
	}
	
	public void toggleReady(UserSession user) {
		if (user == null || user.getLobby() == null) {
			return;
		}
		Lobby lobby = user.getLobby();
		if (user.isReady()) {
			user.setReady(false);
			refreshLobbyUserList(lobby);
			for (UserSession u : lobby.getPlayers()) {
				responder.chatMessageToRoom(u.getSession(), ColorUtil.system(user.getUsername() + " is not ready."), lobby.getName());
			}
		} else {
			user.setReady(true);
			refreshLobbyUserList(lobby);
			for (UserSession u : lobby.getPlayers()) {
				responder.chatMessageToRoom(u.getSession(), ColorUtil.system(user.getUsername() + " is ready."), lobby.getName());
			}
		}
	}
	
	public void toggleForce(UserSession user) {
		if (user == null || user.getLobby() == null) {
			return;
		}
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setForcestart(!lobby.isForcestart());
			if (lobby.isForcestart()) {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Force start is enabled for this song."), lobby.getName());
				}
			} else {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Force start is disabled."), lobby.getName());
				}
			}
		} else {
			responder.chatMessageToRoom(user.getSession(), ColorUtil.system("You can't set force start."), lobby.getName());
		}
	}
	
	public void toggleFreepick(UserSession user) {
		if (user == null || user.getLobby() == null) {
			return;
		}
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setFreepick(!lobby.isFreepick());
			if (lobby.isFreepick()) {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Freepick is enabled. Anyone can pick a song."), lobby.getName());
				}
			} else {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Freepick is disabled. Only Operators and the Owner can pick a song."), lobby.getName());
				}
			}
		}
	}
	
	public void toggleFreerate(UserSession user) {
		if (user == null || user.getLobby() == null) {
			return;
		}
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setFreerate(!lobby.isFreerate());
			if (lobby.isFreerate()) {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Freerate is enabled. You may pick any rate to play."), lobby.getName());
				}
			} else {
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), ColorUtil.system("Freerate is disabled. The song selector picks the rate."), lobby.getName());
				}
			}
		}
	}
	
	public void updateLobbyGameplay(Lobby lobby) {
		if (lobby == null) {
			return;
		}
		
		LeaderboardResponseMessage response = new LeaderboardResponseMessage(lobby);
		for (UserSession user : lobby.getPlayers()) {
			responder.respond(user.getSession(), "leaderboard", response);
		}
	}
	
	/**
	 * A client talks to the server for the first time to inform of generic client information
	 */
	public void clientHello(WebSocketSession session, HelloMessage msg) {
		UserSession user = sessions.get(session.getId());
		if (user != null) {
			user.setClient(msg.getClient());
			try {
				user.setEttpcVersion(Integer.parseInt(msg.getVersion()));
			} catch (Exception e) {
				user.setEttpcVersion(0);
			}
			user.setPacks(new HashSet<>(msg.getPacks()));
		} else {
			m_logger.error("Session could not be found for session {}", session.getId());
		}
	}
	
}
