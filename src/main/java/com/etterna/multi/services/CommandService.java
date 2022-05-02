package com.etterna.multi.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.socket.ettpmessage.payload.ChatMessage;

/**
 * Contains public access to commands
 * Each command function must start with "cmd_"
 */
@Service
public class CommandService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(CommandService.class);
	private static final String CMD_METHOD_PREFIX = "cmd_";
	
	private HashMap<String, Method> commands = new HashMap<>();
	
	@Autowired
	private SessionService sessions;
	
	@Autowired
	private ResponseService responder;
	
	@Autowired
	
	
	@PostConstruct
	private void init() {
		for (final Method m : this.getClass().getDeclaredMethods()) {
			if (m.getName().startsWith(CMD_METHOD_PREFIX)) {
				final String name = m.getName().substring(CMD_METHOD_PREFIX.length());
				m.setAccessible(true);
				commands.put(name, m);
			}
		}
	}
	
	public boolean execute(WebSocketSession session, ChatMessage msg, String cmd, String[] args) {
		final String commandName = cmd.toLowerCase();
		
		final Method commandMethod = commands.get(commandName);
		if (commandMethod == null) {
			return false;
		}
		
		CommandData data = new CommandData();
		data.setArgs(args);
		data.setMsgData(msg);
		data.setSession(session);
		
		try {
			commandMethod.invoke(this, data);
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	
	
	
	private class CommandData {
		private String[] args;
		private ChatMessage msgData;
		private WebSocketSession session;
		public String[] getArgs() {
			return args;
		}
		public void setArgs(String[] args) {
			this.args = args;
		}
		public ChatMessage getMsgData() {
			return msgData;
		}
		public void setMsgData(ChatMessage msgData) {
			this.msgData = msgData;
		}
		public WebSocketSession getSession() {
			return session;
		}
		public void setSession(WebSocketSession session) {
			this.session = session;
		}
	}

}
