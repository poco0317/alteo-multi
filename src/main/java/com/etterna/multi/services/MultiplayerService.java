package com.etterna.multi.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.etterna.multi.socket.ettpmessage.server.payload.EnterRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LeaderboardResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LobbyUserlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.RoomDTO;
import com.etterna.multi.socket.ettpmessage.server.payload.RoomlistResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.SelectChartResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.StartChartResponseMessage;

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
	
	private Set<String> logins = ConcurrentHashMap.newKeySet();
	
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

		m_logger.debug("Finished pinging sessions - {} still alive", sessionService.getSessionCount());
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
				user.goOffline();
				
				m_logger.info("Killed multiplayer session - {} - {}", session.getId(), user.getUsername());
			}
		}
	}
	
	public void killSessionByUsername(String username) {
		do {
			UserSession user = sessionService.getByUsername(username);
			if (user != null && user.getUsername() != null && user.getUsername().equalsIgnoreCase(username)) {
				user.goOffline();
				
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
		if (sender == null || recipientName == null || message == null || sender.getUsername() == null || recipientName.isBlank() || message.isBlank()) {
			return;
		}
		
		UserSession recipient = sessionService.getByUsername(recipientName);
		if (recipient != null) {
			responder.userChatPrivatelyToUser(sender, recipient, message); 
		} else {
			responder.systemNoticeToUserInPrivate(sender, "No user by the name of '"+recipientName+"' is online.", recipientName);
		}
	}
	
	public boolean createLoginSession(String username, String password, WebSocketSession session) {
		boolean allowed = loginService.login(username, password);
		
		if (!allowed) {
			// user gave a wrong password or there was some other error
			return false;
		}
		
		if (logins.contains(username)) {
			// user is already logged in
			// kill previous session
			killSessionByUsername(username);
		}
		
		UserSession user = sessionService.get(session);
		if (user != null) {
			// login
			user.setUsername(username);
			user.goOnline();
			logins.add(username);
			sendUserListToUser(user);
			sendRoomListToUser(user);
		} else {
			// user is not connected?
			m_logger.error("Session could not be found for user {} - session {}", username, session.getId());
		}
		return true;
	}
	
	public boolean createLobby(WebSocketSession session, EnterRoomMessage msg) {
		return lobbyService.createLobby(session, msg);
	}
	
	public boolean createLobby(WebSocketSession session, CreateRoomMessage msg) {
		return lobbyService.createLobby(session, msg);
	}
	
	public void updateLobbyState(Lobby lobby) {
		if (lobby == null) return;
		lobbyService.updateLobbyState(lobby);
	}
	
	public boolean lobbyExists(String name) {
		return lobbyService.getLobbyByName(name) != null;
	}
	
	public boolean tryToJoinLobby(UserSession user, String name, String password) {
		Lobby lobby = lobbyService.getLobbyByName(name);
		if (lobby != null) {
			if (lobby.isBanned(user.getUsername())) {
				responder.systemNoticeToUserInGlobalChat(user, "You are banned from '"+name+"'");
				return false;
			}
			
			if (lobby.getPassword() == null || lobby.getPassword().isBlank()) {
				// no password, come on in
				user.enterLobby(lobby);
				return true;
			} else {
				if (lobby.checkPassword(password)) {
					// success password
					user.enterLobby(lobby);
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
			responder.systemNoticeToUserInGlobalChat(user, "Left room '"+lobby.getName()+"'");
		}
	}
	
	public void sendUserListToUser(UserSession recipient) {
		LobbyUserlistResponseMessage response = new LobbyUserlistResponseMessage(sessionService.getLoggedInSessions());
		responder.respond(recipient.getSession(), "lobbyuserlist", response);
	}
	
	public void sendRoomListToUser(UserSession user) {
		List<RoomDTO> roomdtos = lobbyService.getAllLobbies(true).stream().map(p -> new RoomDTO(p)).collect(Collectors.toList());
		responder.respond(user.getSession(), "roomlist", new RoomlistResponseMessage(roomdtos));
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
		String ratestr = rate != null ? String.format("%.2fx", rate / 1000.0) : "";
		String chatmsg = String.format("%s selected %s (%s) - %s%s",
				user.getUsername(),
				chart.getTitle(),
				chart.getDifficulty(),
				ratestr,
				msg.getPack() != null ? " - "+msg.getPack() : "");
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
		lobby.broadcastUpdate();
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
		user.toggleReady();
	}
	
	public void toggleForce(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		user.toggleForceStart();
	}
	
	public void toggleFreepick(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		user.toggleFreepick();
	}
	
	public void toggleFreerate(UserSession user) {
		if (user == null || user.getLobby() == null) return;
		user.toggleFreerate();
	}
	
	public void updateLobbyGameplay(Lobby lobby) {
		if (lobby == null) return;
		
		LeaderboardResponseMessage response = new LeaderboardResponseMessage(lobby);
		responder.respondToLobby(lobby, "leaderboard", response);
	}
	
	public void banFromLobby(UserSession executor, String recipient) {
		if (executor == null || executor.getLobby() == null) return;
		
		Lobby lobby = executor.getLobby();
		if (lobby.isOperOrOwner(executor)) {
			UserSession target = sessionService.getByUsername(recipient);
			if (target == null) {
				responder.systemNoticeToUserInRoom(executor, "'"+recipient+"' is not online.", lobby.getName());
				return;
			}
			
			if (target.equals(executor)) {
				responder.systemNoticeToUserInRoom(executor, "You can't kick/ban yourself.", lobby.getName());
				return;
			}
			
			if (!lobby.isOwner(executor) && lobby.isOperOrOwner(target)) {
				// an oper cant kick another oper or the owner
				responder.systemNoticeToUserInRoom(executor, "You can't kick/ban another operator or owner.", lobby.getName());
				return;
			}
			
			lobby.addBannedUser(recipient);
			removeFromLobby(target);
			responder.systemNoticeToLobby(lobby, executor.getUsername() + " banned "+recipient+" from the room.");
		} else {
			responder.systemNoticeToUserInRoom(executor, "You don't have permission to kick/ban a user.", lobby.getName());
		}
	}
	
	public void unbanFromLobby(UserSession executor, String recipient) {
		if (executor == null || executor.getLobby() == null) return;
		
		Lobby lobby = executor.getLobby();
		if (lobby.isOperOrOwner(executor)) {
			UserSession target = sessionService.getByUsername(recipient);
			if (target == null) {
				responder.systemNoticeToUserInRoom(executor, "'"+recipient+"' is not online.", lobby.getName());
				return;
			}
			
			lobby.unban(recipient);
			responder.systemNoticeToLobby(lobby, executor.getUsername() + " unbanned "+recipient+" from the room.");
		} else {
			responder.systemNoticeToUserInRoom(executor, "You don't have permission to unban a user.", lobby.getName());
		}
	}
	
	public void toggleOperator(UserSession executor, String recipient) {
		if (executor == null || executor.getLobby() == null) return;
		
		Lobby lobby = executor.getLobby();
		if (lobby.isOwner(executor)) {
			UserSession target = sessionService.getByUsername(recipient);
			if (target == null || !executor.getLobby().equals(target.getLobby())) {
				responder.systemNoticeToUserInRoom(executor, "'"+recipient+"' is not in your room.", lobby.getName());
				return;
			}
			
			if (target.equals(executor)) {
				responder.systemNoticeToUserInRoom(executor, "You are already above an operator.", lobby.getName());
				return;
			}
			
			if (lobby.isOwner(target)) {
				responder.systemNoticeToUserInRoom(executor, "'"+recipient+"' is the room owner and cannot be added as operator.", lobby.getName());
				return;
			}
			
			final String added = lobby.toggleOperator(target) ? " added " : " removed ";
			responder.systemNoticeToLobby(lobby, executor.getUsername() + added +recipient+" as an operator.");
		} else {
			responder.systemNoticeToUserInRoom(executor, "You don't have permission to toggle operator on a user.", lobby.getName());
		}
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
