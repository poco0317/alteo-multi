package com.etterna.multi.services;

import java.util.HashSet;
import java.util.List;
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
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.client.payload.CreateRoomMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.EnterRoomMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.HelloMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.SelectChartMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.StartChartMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.ChartDTO;
import com.etterna.multi.socket.ettpmessage.server.payload.DeleteRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.EnterRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LeaderboardResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LobbyUserlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LobbyUserlistUpdateResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.NewRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.PacklistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.RoomDTO;
import com.etterna.multi.socket.ettpmessage.server.payload.RoomlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.SelectChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.StartChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.UpdateRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.UserlistResponseMessage;

@Service
public class MultiplayerService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(MultiplayerService.class);

	@Autowired
	private UserLoginService loginService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private LobbyService lobbyService;
	
	@Autowired
	private EttpResponseMessageService responder;
	
	private ConcurrentHashMap<String, Object> logins = new ConcurrentHashMap<>();
	private static final Object NOTHING = new Object();
	
	@Scheduled(fixedDelay = SessionService.MILLIS_BETWEEN_STANDARD_HEARTBEAT)
	private void keepaliveSessions() {
		if (sessionService.getSessionCount() == 0) return;
		m_logger.debug("Pinging sessions - {} sessions", sessionService.getSessionCount());

		List<UserSession> deadSessions = sessionService.getSessionsWithOldPing();
		for (UserSession session : deadSessions) {
			killSession(session.getSession());
		}
		
		// ping all alive sessions to keep them alive, if they are getting old and unresponsive
		final long now = System.currentTimeMillis();
		sessionService.executeForAllSessions(user -> {
			if (user.getLastPing() + SessionService.MILLIS_BETWEEN_STANDARD_HEARTBEAT <= now) {
				responder.respond(user, "ping", null);
			}
		});

		m_logger.debug("Finished pinging sessions - {} remaining", sessionService.getSessionCount());
	}
	
	/**
	 * Send data to all users
	 */
	public <T> void respondAllSessions(String messageType, T ettpMessageResponse) {
		sessionService.executeForAllSessions(session -> {
			responder.respond(session, messageType, ettpMessageResponse);
		});
	}
	
	/**
	 * Reset the ping time for a session to make sure it stays alive
	 */
	public void pingSession(WebSocketSession session) {
		sessionService.ping(session);
	}
	
	/**
	 * Maintain UserSession data when closing a socket session
	 */
	public void killSession(WebSocketSession session) {
		UserSession user = sessionService.get(session);
		if (sessionService.kill(session)) {
			if (user.getUsername() != null) {
				logins.remove(user.getUsername());
				broadcastConnectedUserLobbylistRemoval(user);
				
				Lobby lobby = lobbyService.getLobbyByUserSession(user);
				if (lobby != null) {
					lobbyService.removeFromLobby(user, lobby);
				}
				
				m_logger.info("Killed multiplayer session - {} - {}", session.getId(), user.getUsername());
			}
		}
	}
	
	public void killSessionByUsername(String username) {
		do {
			UserSession user = sessionService.getByUsername(username);
			if (user != null && user.getUsername() != null && user.getUsername().equalsIgnoreCase(username)) {
				broadcastConnectedUserLobbylistRemoval(user);
				
				Lobby lobby = lobbyService.getLobbyByUserSession(user);
				if (lobby != null) {
					lobbyService.removeFromLobby(user, lobby);
				}
				
				m_logger.info("Killed ws session by user - {}", username);
			}
		} while (sessionService.killByUsername(username));
	}
	
	/**
	 * Send a message to the lobby tab of every connected user
	 */
	public void chatToMainLobby(UserSession sender, String message) {
		if (sender == null || sender.getUsername() == null) {
			m_logger.warn("Attempted to send message to everyone using empty name - Message: {}", message);
			return;
		}
		
		sessionService.executeForAllLoggedInSessions(session -> {
			responder.userChatToGlobalChat(session, sender.getUsername(), message);
		});
	}
	
	public void systemMessageToGlobalChat(String message) {
		sessionService.executeForAllLoggedInSessions(session -> {
			responder.systemNoticeToUserInGlobalChat(session, message);
		});
	}
	
	/**
	 * Privately send a message to a sender and a receiver
	 */
	public void privateMessage(UserSession sender, String recipientName, String message) {
		if (sender == null || recipientName == null || message == null || sender.getUsername() == null) {
			return;
		}
		
		UserSession recipient = sessionService.getByUsername(recipientName);
		if (recipient != null) {
			responder.userChatPrivatelyToUser(sender, recipient, message); 
		} else {
			responder.systemNoticeToUserInPrivate(sender, "Could not find user '"+recipientName+"'", "");
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
		
		UserSession user = sessionService.get(session);
		if (user != null) {
			// login
			user.setUsername(username);
			logins.put(username, NOTHING);
			sendUserListToUser(user);
			broadcastConnectedUserLobbylistAddition(user);
			sendRoomListToUser(user);
		} else {
			// user is not connected?
			m_logger.error("Session could not be found for user {} - session {}", username, session.getId());
		}
		return true;
	}
	
	public boolean createLobby(WebSocketSession session, EnterRoomMessage msg) {
		if (lobbyService.createLobby(session, msg)) {
			Lobby lobby = lobbyService.getLobbyBySocketSession(session);
			broadcastRoomCreation(lobby);
			sendUserListToLobby(lobby);
			return true;
		}
		return false;
	}
	
	public boolean createLobby(WebSocketSession session, CreateRoomMessage msg) {
		if (lobbyService.createLobby(session, msg)) {
			Lobby lobby = lobbyService.getLobbyBySocketSession(session);
			broadcastRoomCreation(lobby);
			sendUserListToLobby(lobby);
			return true;
		}
		return false;
	}
	
	public void updateLobbyState(Lobby lobby) {
		if (lobby == null) return;
		
		lobbyService.updateLobbyState(lobby);
		sendUserListToLobby(lobby);
		broadcastLobbyUpdate(lobby);
	}
	
	public void enterLobby(UserSession user, Lobby lobby) {
		lobbyService.enterLobby(user, lobby);
		
		// put the user in the room
		responder.respond(user.getSession(), "enterroom", new EnterRoomResponseMessage(true));
		
		// tell everyone there that they joined
		responder.systemNoticeToLobby(lobby, user.getUsername()+" joined.");
		
		// tell everyone else what state the lobby is in and who is in there
		respondAllSessions("updateroom", new UpdateRoomResponseMessage(lobby));
		sendUserListToLobby(lobby);
		
		// update the pack list for the lobby users
		sendPackListToLobby(lobby);
		
		// if there was a chart selected, put the user on it
		if (lobby.getChart() != null) {
			responder.respond(user.getSession(), "selectchart", null);
		}
	}
	
	public boolean lobbyExists(String name) {
		return lobbyService.getLobbyByName(name) != null;
	}
	
	public boolean tryToJoinLobby(UserSession user, String name, String password) {
		Lobby lobby = lobbyService.getLobbyByName(name);
		if (lobby != null) {
			if (lobby.getPassword() == null || lobby.getPassword().isBlank()) {
				// no password, come on in
				enterLobby(user, lobby);
				return true;
			} else {
				if (lobby.checkPassword(password)) {
					// success password
					enterLobby(user, lobby);
					return true;
				} else {
					// failed password
					responder.respond(user.getSession(), "enterroom", new EnterRoomResponseMessage(false));
					responder.systemNoticeToUserInGlobalChat(user, ColorUtil.system("Incorrect password."));
					return false;
				}
			}
		}
		return false;
	}
	
	public void removeFromLobby(UserSession user) {
		Lobby lobby = lobbyService.getLobbyByUserSession(user);
		if (lobby != null) {
			lobbyService.removeFromLobby(user, lobby);
			
			if (lobby.getPlayers().isEmpty()) {
				respondAllSessions("deleteroom", new DeleteRoomResponseMessage(lobby));
			} else {
				sendUserListToLobby(lobby);
				responder.systemNoticeToLobby(lobby, user.getUsername() + " left.");
			}
			responder.systemNoticeToUserInGlobalChat(user, "Left room '"+lobby.getName()+"'");
		}
		
	}
	
	public void broadcastLobbyUpdate(Lobby lobby) {
		if (lobby != null) {
			respondAllSessions("updateroom", new UpdateRoomResponseMessage(lobby));
		}
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
	
	public void broadcastRoomList() {
		List<RoomDTO> roomdtos = lobbyService.getAllLobbies(true).stream().map(p -> new RoomDTO(p)).collect(Collectors.toList());
		respondAllSessions("roomlist", new RoomlistResponseMessage(roomdtos));
	}
	
	public void sendUserListToUser(UserSession recipient) {
		LobbyUserlistResponseMessage response = new LobbyUserlistResponseMessage(sessionService.getLoggedInSessions());
		responder.respond(recipient.getSession(), "lobbyuserlist", response);
	}
	
	public void sendRoomListToUser(UserSession user) {
		List<RoomDTO> roomdtos = lobbyService.getAllLobbies(true).stream().map(p -> new RoomDTO(p)).collect(Collectors.toList());
		responder.respond(user.getSession(), "roomlist", new RoomlistResponseMessage(roomdtos));
	}
	
	/**
	 * For rooms/lobbies only
	 */
	public void sendUserListToLobby(Lobby lobby) {
		if (lobby == null) return;
		UserlistResponseMessage response = new UserlistResponseMessage(lobby);
		responder.respondToLobby(lobby, "userlist", response);
	}
	
	public void sendPackListToLobby(Lobby lobby) {
		if (lobby == null) return;
		PacklistResponseMessage response = new PacklistResponseMessage(lobby);
		responder.respondToLobby(lobby, "packlist", response);
	}
	
	public void selectChart(UserSession user, StartChartMessage msg) {
		selectChart(user, new SelectChartMessage(msg));
	}
	
	public void selectChart(UserSession user, SelectChartMessage msg) {
		Chart chart = new Chart(msg);
		chart.setPickedBy(user.getUsername());
		Lobby lobby = user.getLobby();
		if (lobby == null) return;
		lobby.setChart(chart);

		SelectChartResponseMessage response = user.getLobby().serializeChart(chart);
		Integer rate = response.getChart().getRate();
		String ratestr = rate != null ? String.format("%.2f", rate / 1000.0) : "";
		String chatmsg = String.format("%s selected %s (%s) %s %s",
				user.getUsername(),
				chart.getTitle(),
				chart.getDifficulty(),
				ratestr,
				msg.getPack() != null ? msg.getPack() : "");
		responder.respondToLobby(lobby, "selectchart", response);
		responder.systemNoticeToLobby(lobby, chatmsg);
	}
	
	public void startChart(UserSession user, StartChartMessage msg) {
		Lobby lobby = user.getLobby();
		if (lobby.isCountdown()) {
			String errors = lobby.allReady(user);
			if (errors != null && !errors.isBlank()) {
				responder.systemNoticeToLobby(lobby, errors);
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
		if (lobby == null) return;
		Chart chart = new Chart(msg);
		
		ChartDTO newch = lobby.getChartDTO(chart);
		ChartDTO oldch = lobby.getChartDTO(lobby.getChart());
		if (lobby.getChart() == null || !newch.equals(oldch)) {
			selectChart(user, msg);
			return;
		}
		
		String errors = lobby.allReady(user);
		if (errors != null && !errors.isBlank()) {
			responder.systemNoticeToLobby(lobby, errors);
			return;
		}
		
		lobbyService.startChart(lobby, chart);
		StartChartResponseMessage response = new StartChartResponseMessage(newch);
		responder.respondToLobby(lobby, "startchart", response);
		responder.systemNoticeToLobby(lobby, "Starting "+chart.getTitle());
	}
	
	public void startCountdown(UserSession user, StartChartMessage msg) {
		Lobby lobby = user.getLobby();
		lobbyService.startCountdown(lobby, (left, finished) -> {
			if (finished) {
				responder.systemNoticeToLobby(lobby, "Starting song.");
				startChartInternal(user, msg);
			} else {
				responder.systemNoticeToLobby(lobby, "Starting in "+left+" seconds.");
			}
		});
	}
	
	public void stopCountdown(Lobby lobby) {
		lobbyService.stopCountdown(lobby);
	}
	
	public void toggleReady(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		Lobby lobby = user.getLobby();
		if (user.isReady()) {
			user.setReady(false);
			sendUserListToLobby(lobby);
			responder.systemNoticeToLobby(lobby, user.getUsername() + " is not ready.");
		} else {
			user.setReady(true);
			sendUserListToLobby(lobby);
			responder.systemNoticeToLobby(lobby, user.getUsername() + " is ready.");
		}
	}
	
	public void toggleForce(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setForcestart(!lobby.isForcestart());
			if (lobby.isForcestart()) {
				responder.systemNoticeToLobby(lobby, "Force start is enabled for this song.");
			} else {
				responder.systemNoticeToLobby(lobby, "Force start is disabled.");
			}
		} else {
			responder.systemNoticeToUserInPrivate(user, "You can't set force start.", user.getLobby().getName());
		}
	}
	
	public void toggleFreepick(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setFreepick(!lobby.isFreepick());
			if (lobby.isFreepick()) {
				responder.systemNoticeToLobby(lobby, "Freepick is enabled. Anyone can pick a song.");
			} else {
				responder.systemNoticeToLobby(lobby, "Freepick is disabled. Only Operators and the Owner can pick a song.");
			}
		} else {
			responder.systemNoticeToUserInPrivate(user, "You can't set free song selection.", user.getLobby().getName());
		}
	}
	
	public void toggleFreerate(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		Lobby lobby = user.getLobby();
		if (lobby.isOperOrOwner(user)) {
			lobby.setFreerate(!lobby.isFreerate());
			if (lobby.isFreerate()) {
				responder.systemNoticeToLobby(lobby, "Freerate is enabled. You may pick any rate to play.");
			} else {
				responder.systemNoticeToLobby(lobby, "Freerate is disabled. The song selector picks the rate.");
			}
		} else {
			responder.systemNoticeToUserInPrivate(user, "You can't set free rate selection.", user.getLobby().getName());
		}
	}
	
	public void updateLobbyGameplay(Lobby lobby) {
		if (lobby == null) return;
		
		LeaderboardResponseMessage response = new LeaderboardResponseMessage(lobby);
		responder.respondToLobby(lobby, "leaderboard", response);
	}
	
	/**
	 * A client talks to the server for the first time to inform of generic client information
	 */
	public void clientHello(WebSocketSession session, HelloMessage msg) {
		UserSession user = sessionService.get(session);
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
