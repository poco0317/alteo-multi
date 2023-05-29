package com.etterna.multi.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.etterna.multi.data.GameLobby;
import com.etterna.multi.data.LobbyMessage;
import com.etterna.multi.data.LobbyScore;
import com.etterna.multi.data.UserLogin;
import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.repo.GameLobbyRepository;
import com.etterna.multi.repo.LobbyMessageRepository;
import com.etterna.multi.repo.LobbyScoreRepository;
import com.etterna.multi.socket.ettpmessage.client.payload.ScoreMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LobbyAuditingDispatch {
	
	@Autowired
	private GameLobbyRepository lobbies;
	
	@Autowired
	private LobbyMessageRepository messages;
	
	@Autowired
	private LobbyScoreRepository scores;
	
	@Autowired
	private UserLoginService logins;
	
	@EventListener
	@Transactional
	public void handleApplicationReadyEvent(ApplicationReadyEvent evt) {
		m_logger.info("Server started - resetting all lobbies to closed state");
		
		lobbies.closeAllLobbies();
		m_logger.info("Finished resetting lobbies to closed state");
	}
	
	@Transactional
	public void roomCreation(UserSession user) {
		m_logger.trace("Recording user room creation");
		
		Lobby l = user.getLobby();
		GameLobby newLobby = new GameLobby();
		UserLogin login = logins.get(user.getUsername());
		newLobby.setCreator(login);
		newLobby.setDescription(l.getDescription());
		newLobby.setName(l.getName());
		newLobby.setPassworded(l.getPassword() != null && !l.getPassword().isBlank());
		newLobby.setActive(true);
		Set<UserLogin> users = new HashSet<>();
		users.add(login);
		newLobby.setUsers(users);
		l.setDbGameLobby(newLobby);
		newLobby = lobbies.save(newLobby);
	}
	
	@Transactional
	public void roomDeletion(Lobby lobby) {
		m_logger.trace("Recording user room ending");
		
		GameLobby newLobby = lobby.getDbGameLobby();
		newLobby.setActive(false);
		newLobby = lobbies.save(newLobby);
	}
	
	@Transactional
	public void roomParticipant(UserSession user) {
		m_logger.trace("Recording user room participation");
		
		GameLobby l = user.getLobby().getDbGameLobby();
		l.getUsers().add(logins.get(user.getUsername()));
		l = lobbies.save(l);
	}
	
	@Transactional
	public void roomMessage(Lobby lobby, String sender, String content) {
		m_logger.trace("Recording room message");
		
		LobbyMessage msg = new LobbyMessage();
		msg.setContent(content);
		msg.setSender(sender);
		msg.setSent(new Date());
		msg.setLobby(lobby.getDbGameLobby());
		msg = messages.save(msg);
	}
	
	@Transactional
	public void roomScore(UserSession user, ScoreMessage scoreMsg) {
		m_logger.trace("Recording room score");
		
		LobbyScore score = scoreMsg.toLobbyScore();
		score.setLobby(user.getLobby().getDbGameLobby());
		score.setUser(logins.get(user.getUsername()));
		score = scores.save(score);
	}

}
