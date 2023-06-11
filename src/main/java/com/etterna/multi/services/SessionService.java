package com.etterna.multi.services;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import com.etterna.multi.data.state.UserSession;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SessionService {
	
	@Autowired
	private ApplicationContext ctx;
	
	public static final long MAX_MILLIS_BETWEEN_HEARTBEATS = 30L * 1000L; // 30sec
	public static final long MILLIS_BETWEEN_STANDARD_HEARTBEAT = 10L * 1000L; // 10sec
	public static final int WS_SEND_LIMIT_MILLIS = 1000 * 45; // 45sec
	public static final int WS_MSG_QUEUE_BUFFER_SIZE_BYTES = 1024 * 1024; // 1mb

	// session ids to UserSessions
	// UserSessions dont necessarily have any user logged in
	private ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

	@Scheduled(fixedDelay = 1000L * 10L)
	private void maintainSessions() {
		if (getSessionCount() == 0) return;
		m_logger.debug("Running session maintenance - {} sessions", getSessionCount());

		// prune forgotten dead sessions and logins
		Iterator<Entry<String, UserSession>> it = sessions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UserSession> e = it.next();
			UserSession user = e.getValue();
			if (user.getSession() != null && !user.getSession().isOpen()) {
				it.remove();
			}
		}

		m_logger.debug("Session maintenance complete - {} sessions left", getSessionCount());
	}

	public int getSessionCount() {
		return sessions.size();
	}
	
	/**
	 * Get a user session from the connection
	 */
	public UserSession get(WebSocketSession session) {
		return sessions.get(session.getId());
	}
	
	public UserSession getByUsername(String username) {
		Iterator<Entry<String, UserSession>> it = sessions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UserSession> e = it.next();
			UserSession u = e.getValue();
			if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
				return u;
			}
		}
		return null;
	}
	
	public List<UserSession> getLoggedInSessions() {
		return sessions.values().stream().filter(p -> p.getUsername() != null && !p.getUsername().isBlank()).collect(Collectors.toList());
	}
	
	public List<UserSession> getSessionsWithOldPing() {
		return sessions.values().stream().filter(session -> !wasPingRecent(session)).collect(Collectors.toList());
	}
	
	private static boolean wasPingRecent(UserSession user) {
		return user.getLastPing() + MAX_MILLIS_BETWEEN_HEARTBEATS > System.currentTimeMillis();
	}

	public void register(WebSocketSession session) {
		UserSession user = ctx.getBean(UserSession.class);
		user.setSession(new ConcurrentWebSocketSessionDecorator(session, WS_SEND_LIMIT_MILLIS, WS_MSG_QUEUE_BUFFER_SIZE_BYTES));
		sessions.put(session.getId(), user);
	}

	public void ping(WebSocketSession session) {
		UserSession user = get(session);
		if (user != null) {
			user.setLastPing(System.currentTimeMillis());
		}
	}

	/**
	 * Kills the connection and related data.
	 * <br>Returns true if the session was a recorded session
	 */
	public boolean kill(WebSocketSession session) {
		if (session == null) return false;
		
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
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Kills the connection and related data for the first session found by username.
	 * <br>Returns true if found
	 */
	public boolean killByUsername(String username) {
		Iterator<Entry<String, UserSession>> it = sessions.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UserSession> e = it.next();
			UserSession u = e.getValue();
			if (u.getUsername() != null && u.getUsername().equalsIgnoreCase(username)) {
				it.remove();
				if (kill(u.getSession())) {
					m_logger.info("Killed ws session by user - {}", username);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	public void executeForAllSessions(Consumer<UserSession> f) {
		for (UserSession session : sessions.values()) {
			f.accept(session);
		}
	}
	
	public void executeForAllLoggedInSessions(Consumer<UserSession> f) {
		for (UserSession session : sessions.values()) {
			if (session.getUsername() != null) {
				f.accept(session);
			}
		}
	}

}
