package com.etterna.multi.data.state;

import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.services.EttpResponseMessageService;
import com.etterna.multi.services.LobbyAuditingDispatch;
import com.etterna.multi.services.LobbyService;
import com.etterna.multi.services.MultiplayerService;
import com.etterna.multi.socket.ettpmessage.server.payload.EnterRoomResponseMessage;
import com.etterna.multi.socket.ettpmessage.server.payload.LobbyUserlistUpdateResponseMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Getter @Setter
@Slf4j
public class UserSession {
	
	@Autowired
	private MultiplayerService multiplayer;
	
	@Autowired
	private LobbyService lobbyService;
	
	@Autowired
	private EttpResponseMessageService messaging;
	
	@Autowired
	private LobbyAuditingDispatch auditDispatch;
	
	private String username;
	private WebSocketSession session;
	private long lastPing = System.currentTimeMillis();
	private int ettpcVersion;
	private String client;
	private Set<String> packs;
	private PlayerState state = PlayerState.READY;
	private boolean isReady = false;
	private Lobby lobby;
	
	private double gameplayWife;
	private String gameplayJudgments;
	
	public void toggleReady() {
		if (getLobby() == null) return;
		if (isReady()) {
			setReady(false);
			lobby.broadcastUserlistUpdate();
			messaging.systemNoticeToLobby(lobby, getUsername() + " is not ready.");
		} else {
			setReady(true);
			lobby.broadcastUserlistUpdate();
			messaging.systemNoticeToLobby(lobby, getUsername() + " is ready.");
		}
	}
	
	public void toggleForceStart() {
		if (getLobby() == null) return;
		Lobby lobby = getLobby();
		if (lobby.isOperOrOwner(this)) {
			lobby.setForcestart(!lobby.isForcestart());
			if (lobby.isForcestart()) {
				messaging.systemNoticeToLobby(lobby, getUsername() + " enabled force start for this song.");
			} else {
				messaging.systemNoticeToLobby(lobby, getUsername() + " disabled force start.");
			}
		} else {
			messaging.systemNoticeToUserInRoom(this, "You can't set force start. Must be /op'd or owner.", lobby.getName());
		}
	}
	
	public void toggleFreepick() {
		if (getLobby() == null) return;
		Lobby lobby = getLobby();
		if (lobby.isOperOrOwner(this)) {
			lobby.setFreepick(!lobby.isFreepick());
			if (lobby.isFreepick()) {
				messaging.systemNoticeToLobby(lobby, getUsername() + " enabled freepick. Anyone can pick a song.");
			} else {
				messaging.systemNoticeToLobby(lobby, getUsername() + " disabled freepick. Only Operators and the Owner can pick a song.");
			}
		} else {
			messaging.systemNoticeToUserInRoom(this, "You can't set free song selection. Must be /op'd or owner.", lobby.getName());
		}
	}
	
	public void toggleFreerate() {
		if (getLobby() == null) return;
		Lobby lobby = getLobby();
		if (lobby.isOperOrOwner(this)) {
			lobby.setFreerate(!lobby.isFreerate());
			if (lobby.isFreerate()) {
				messaging.systemNoticeToLobby(lobby, getUsername() + " enabled freerate. You may pick any rate to play.");
			} else {
				messaging.systemNoticeToLobby(lobby, getUsername() + " disabled freerate. The song selector picks the rate.");
			}
		} else {
			messaging.systemNoticeToUserInRoom(this, "You can't set free rate selection. Must be /op'd or owner.", lobby.getName());
		}
	}
	
	/**
	 * Force a user to go "offline"
	 * Also triggers {@link #leaveLobby(boolean)}
	 */
	public void goOffline() {
		if (getUsername() == null) {
			return;
		}
		
		m_logger.info("User {} {} is going offline", getUsername(), session.getId());
		
		leaveLobby(true);
		
		LobbyUserlistUpdateResponseMessage response = new LobbyUserlistUpdateResponseMessage();
		response.addOff(getUsername());
		multiplayer.respondAllSessions("lobbyuserlistupdate", response);
	}
	
	public void goOnline() {
		if (getUsername() == null) {
			return;
		}
		
		m_logger.info("User {} {} is coming online", getUsername(), session.getId());
		
		LobbyUserlistUpdateResponseMessage response = new LobbyUserlistUpdateResponseMessage();
		response.addOn(getUsername());
		multiplayer.respondAllSessions("lobbyuserlistupdate", response);
	}
	
	public void leaveLobby(boolean forced) {
		if (lobby == null) {
			return;
		}
		
		m_logger.info("User {} {} is leaving lobby '{}' (forced {})", username, session.getId(), lobby.getName(), forced);
		
		lobbyService.removeFromLobby(this, getLobby());
	}
	
	public void enterLobby(Lobby lobby) {
		lobbyService.enterLobby(this, lobby);
		
		// put the user in the room
		messaging.respond(getSession(), "enterroom", new EnterRoomResponseMessage(true));
		
		// tell everyone there that they joined
		messaging.systemNoticeToLobby(lobby, getUsername()+" joined.");
		
		// tell everyone else what state the lobby is in and who is in there
		lobbyService.updateLobbyState(lobby);
		
		// update the pack list for the lobby users
		lobby.broadcastPacklist();
		
		// if there was a chart selected, put the user on it
		if (lobby.getChart() != null) {
			messaging.respond(getSession(), "selectchart", null);
		}
	}
	
	
	
	@Override
	public int hashCode() {
		return Objects.hash(session, username);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSession other = (UserSession) obj;
		return session.equals(other.session);
	}

}
