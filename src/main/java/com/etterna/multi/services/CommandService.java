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
import com.etterna.multi.socket.ettpmessage.ChatMessageType;
import com.etterna.multi.socket.ettpmessage.client.payload.ChatMessage;

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
	private MultiplayerService multiplayer;
	
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
		
		try {
			if (commandMethod.getParameterCount() == 1) {
				commandMethod.invoke(this, data);
			} else if (commandMethod.getParameterCount() == 2) {
				UserSession user = sessions.get(session);
				commandMethod.invoke(this, data, user);
			} else {
				m_logger.error("Command {} attempted with bad argument count", cmd);
				return false;
			}
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
	
	/**
	 * Send a chat message to either public lobby, private message, or player's room based on attributes
	 */
	private void chat(UserSession user, String tab, int msgType, String message) {
		if (tab == null || tab.isBlank() || msgType == ChatMessageType.LOBBY.num()) {
			// this might be going to the public lobby
			multiplayer.chatToMainLobby(user, message);
		} else {
			// a tab was specified
			if (msgType == ChatMessageType.ROOM.num()) {
				// this is a dm
				multiplayer.privateMessage(user, tab, message);
			} else {
				// this is a room message
				responder.userChatToLobby(user, message);
			}
		}
	}
	
	
	void cmd_pm(CommandData data, UserSession user) {
		List<String> args = data.getArgs();
		if (args == null || args.size() == 0) {
			return;
		}
		String recipient = args.get(0);
		String message = Strings.join(args.subList(1, args.size()), ' ');
		
		multiplayer.privateMessage(user, recipient, message);
	}
	
	void cmd_wave(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String wave = "( * ^ *) ノシ";
		chat(user, tab, msgType, wave);
	}
	
	void cmd_lenny(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String lenny = "( ͡° ͜ʖ ͡°)";
		chat(user, tab, msgType, lenny);
	}
	
	void cmd_shrug(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String shrug = "¯\\_(ツ)_/";
		chat(user, tab, msgType, shrug);
	}
	
	void cmd_help(CommandData data, UserSession user) {
		responder.systemNoticeToUser(user, "I didn't write help yet", "");
	}
	
	void cmd_ready(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInMainLobby(user, "You aren't in a lobby");
			return;
		}
		multiplayer.toggleReady(user);
	}
	
	void cmd_force(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInMainLobby(user, "You aren't in a lobby");
		}
		multiplayer.toggleForce(user);
	}
	
	void cmd_free(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInMainLobby(user, "You aren't in a lobby");
		}
		multiplayer.toggleFreepick(user);
	}
	
	void cmd_freerate(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInMainLobby(user, "You aren't in a lobby");
		}
		multiplayer.toggleFreerate(user);
	}
	
	
	private class CommandData {
		private List<String> args;
		private ChatMessage msgData;
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
	}

}
