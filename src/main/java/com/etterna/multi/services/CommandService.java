package com.etterna.multi.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.UserSession;
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
	private EttpResponseMessageService responder;
	
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
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	
	void cmd_pm(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		List<String> args = data.getArgs();
		if (args == null || args.size() == 0) {
			return;
		}
		String recipient = args.get(0);
		String message = Strings.join(args.subList(1, args.size()), ' ');
		
		sessions.privateMessage(user, recipient, message);
	}
	
	void cmd_wave(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		responder.chatMessageToRoom(user.getSession(), ColorUtil.colorize(user.getUsername(), ColorUtil.colorUser(user)) + ": ( * ^ *) ノシ", data.getMsgData().getTab());
	}
	
	void cmd_lenny(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		responder.chatMessageToRoom(user.getSession(), ColorUtil.colorize(user.getUsername(), ColorUtil.colorUser(user)) + ": ( ͡° ͜ʖ ͡°)", data.getMsgData().getTab());
	}
	
	void cmd_shrug(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		responder.chatMessageToRoom(user.getSession(), ColorUtil.colorize(user.getUsername(), ColorUtil.colorUser(user)) + ": ¯\\_(ツ)_/", data.getMsgData().getTab());
	}
	
	void cmd_help(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		responder.chatMessageToUser(user.getSession(), ColorUtil.system("I didnt write help yet"));
	}
	
	void cmd_ready(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		if (user.getLobby() == null) {
			responder.chatMessageToUser(user.getSession(), "You aren't in a lobby");
			return;
		}
		sessions.toggleReady(user);
	}
	
	void cmd_force(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		if (user.getLobby() == null) {
			responder.chatMessageToUser(user.getSession(), "You aren't in a lobby");
		}
		sessions.toggleForce(user);
	}
	
	void cmd_free(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		if (user.getLobby() == null) {
			responder.chatMessageToUser(user.getSession(), "You aren't in a lobby");
		}
		sessions.toggleFreepick(user);
	}
	
	void cmd_freerate(CommandData data) {
		UserSession user = sessions.getUserSession(data.getSession());
		if (user.getLobby() == null) {
			responder.chatMessageToUser(user.getSession(), "You aren't in a lobby");
		}
		sessions.toggleFreerate(user);
	}
	
	
	private class CommandData {
		private List<String> args;
		private ChatMessage msgData;
		private WebSocketSession session;
		public List<String> getArgs() {
			return args;
		}
		public void setArgs(String[] args) {
			this.args = Arrays.asList(args);
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
