package com.etterna.multi.services;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.payload.HelloMessage;

@Service
public class SessionService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(SessionService.class);

	@Autowired
	private UserLoginService loginService;
	
	// session ids to usersessions
	// usersessions dont necessarily have any user logged in
	private ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Object> logins = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 1000L * 10L)
	private void maintainSessions() {
		if (logins.size() == 0) {
			m_logger.debug("Skipping session maintenance - no sessions");
			return;
		}
		m_logger.info("Running session maintenance - {} sessions - {} logins", sessions.size(), logins.size());
		
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
		
		m_logger.info("Session maintenance complete - {} sessions - {} logins", sessions.size(), logins.size());
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
		} else {
			// user is not connected?
			m_logger.error("Session could not be found for user {} - session {}", username, session.getId());
		}
		return true;
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
			user.setPacks(msg.getPacks());
		} else {
			m_logger.error("Session could not be found for session {}", session.getId());
		}
	}
	
}
