package com.etterna.multi.services;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.LobbyState;
import com.etterna.multi.data.state.PlayerState;
import com.etterna.multi.data.state.SelectionMode;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.client.payload.CreateRoomMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.EnterRoomMessage;

@Service
public class LobbyService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(LobbyService.class);
	
	@Autowired
	private SessionService sessionService;

	// room names to lobbies
	private ConcurrentHashMap<String, Lobby> rooms = new ConcurrentHashMap<>();
	
	/**
	 * Get the lobby by name
	 */
	public Lobby getLobbyByName(String name) {
		return rooms.get(name.toLowerCase());
	}
	
	/**
	 * Get the lobby a user connection is in
	 */
	public Lobby getLobbyBySocketSession(WebSocketSession session) {
		return getLobbyByUserSession(sessionService.get(session));
	}
	
	/**
	 * Get the lobby from a user session
	 */
	public Lobby getLobbyByUserSession(UserSession user) {
		if (user != null) {
			return user.getLobby();
		}
		return null;
	}
	
	public List<Lobby> getAllLobbies(boolean sorted) {
		Stream<Lobby> stream = rooms.values().stream().filter(lobby -> !lobby.getPlayers().isEmpty());
		
		if (sorted) {
			stream = stream.sorted(new Comparator<Lobby>() {
				@Override
				public int compare(Lobby o1, Lobby o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}
		return stream.collect(Collectors.toList());
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
		Lobby lobby = getLobbyByName(name);
		if (lobby != null) {
			return false;
		}
		
		UserSession user = sessionService.get(session);
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
		
		rooms.put(name.toLowerCase(), lobby);
		
		m_logger.info("Created lobby '{}' - desc '{}' - PASSWORDED: {}", lobby.getName(), lobby.getDescription(), lobby.getPassword() != null);
		return true;
	}
	
	public void enterLobby(UserSession user, Lobby lobby) {
		lobby.enter(user);
		user.setState(PlayerState.READY);
		
		m_logger.info("Player {} entered lobby {}", user.getUsername(), lobby.getName());
	}
	
	public void removeFromLobby(UserSession user, Lobby lobby) {
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
		} else {
			// otherwise make sure common packs is updated
			lobby.calcCommonPacks();
		}
		
		m_logger.info("Player {} left lobby {} - {} users left", user.getUsername(), lobby.getName(), lobby.getPlayers().size());
	}
	
	public void updateLobbyState(Lobby lobby) {
		if (lobby == null) return;
		
		lobby.setState(LobbyState.SELECTING);
		lobby.getPlayers().forEach(p -> {
			if (!p.getState().equals(PlayerState.READY)) {
				lobby.setState(LobbyState.INGAME);
			}
		});
		
		if (lobby.getState().equals(LobbyState.SELECTING) && lobby.isPlaying()) {
			lobby.setPlaying(false);
			lobby.setChart(null);
		}
	}
	
}
