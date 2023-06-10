package com.etterna.multi.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
				final CommandAlias cmdAlias = m.getAnnotation(CommandAlias.class);
				m.setAccessible(true);
				commands.put(name, m);
				
				if (cmdAlias != null) {
					for (String alias : cmdAlias.values()) {
						commands.put(alias, m);
					}
				}
			}
		}
	}
	
	private String helpForCommand(String cmd, UserSession user) {
		for (final Method method : CommandService.class.getDeclaredMethods()) {
			if (method.getName().startsWith(CMD_METHOD_PREFIX)) {
				final String name = method.getName().substring(CMD_METHOD_PREFIX.length());
				if (name.equalsIgnoreCase(cmd)) {
					HelpMessage msg = method.getAnnotation(HelpMessage.class);
					if (msg != null) {
						return msg.usage() + " - " + msg.desc();
					}
				}
			}
		}
		return "/" + cmd;
	}
	
	private List<String> helpForAllCommands(UserSession user) {
		List<String> l = new ArrayList<>();
		for (Method method : CommandService.class.getDeclaredMethods()) {
			HelpMessage msg = method.getAnnotation(HelpMessage.class);
			if (msg != null) {
				l.add(msg.usage() + " - " + msg.desc());
			}
		}
		return l;
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
			if (msgType == ChatMessageType.PRIVATE.num()) {
				// this is a dm
				multiplayer.privateMessage(user, tab, message);
			} else {
				// this is a room message
				responder.userChatToLobby(user, message);
			}
		}
	}
	
	@CommandAlias(values = {"dm", "msg"})
	@HelpMessage(desc = "Send a private message to a user in a new private tab", usage = "/pm <username> <message>")
	void cmd_pm(CommandData data, UserSession user) {
		List<String> args = data.getArgs();
		if (args == null || args.size() == 0) {
			return;
		}
		String recipient = args.get(0);
		String message = Strings.join(args.subList(1, args.size()), ' ');
		
		multiplayer.privateMessage(user, recipient, message);
	}
	
	@HelpMessage(desc = "Wave", usage = "/wave")
	void cmd_wave(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String wave = "( * ^ *) ノシ";
		chat(user, tab, msgType, wave);
	}
	
	@HelpMessage(desc = "Lenny", usage = "/lenny")
	void cmd_lenny(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String lenny = "( ͡° ͜ʖ ͡°)";
		chat(user, tab, msgType, lenny);
	}
	
	@HelpMessage(desc = "Shrug", usage = "/shrug")
	void cmd_shrug(CommandData data, UserSession user) {
		final String tab = data.getMsgData().getTab();
		final int msgType = data.getMsgData().getMsgtype();
		final String shrug = "¯\\_(ツ)_/";
		chat(user, tab, msgType, shrug);
	}
	
	void cmd_help(CommandData data, UserSession user) {
		final List<String> args = data.getArgs();
		if (args != null && args.size() >= 1) {
			String helpmsg = helpForCommand(args.get(0), user);
			String msg = "";
			if (helpmsg != null) {
				msg = helpmsg;
			} else {
				msg = "Command '"+args.get(0)+"' not found";
			}
			
			final int msgType = data.getMsgData().getMsgtype();
			final String tab = data.getMsgData().getTab();
			responder.systemNoticeToUserInContext(user, msg, msgType, tab);
			return;
		}
		
		// no args, show all commands
		responder.systemNoticeToUserInPrivate(user, "Here's how it works...", "");
		for (String s : helpForAllCommands(user)) {
			responder.systemNoticeToUserInPrivate(user, s, "");
		}
		
	}
	
	@CommandAlias(values = {"r"})
	@HelpMessage(desc = "Toggle your ready status. Everyone must be ready to start a song", usage = "/ready")
	void cmd_ready(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		multiplayer.toggleReady(user);
	}
	
	@HelpMessage(desc = "Toggle force start for the room. Ready status is ignored", usage = "/force", requiresOper = true)
	void cmd_force(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		multiplayer.toggleForce(user);
	}
	
	@HelpMessage(desc = "Toggle free song selection. When on, anyone can pick a song", usage = "/free", requiresOper = true)
	void cmd_free(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		multiplayer.toggleFreepick(user);
	}
	
	@CommandAlias(values = {"fr"})
	@HelpMessage(desc = "Toggle free rate selection. When on, anyone can pick any music rate", usage = "/freerate", requiresOper = true)
	void cmd_freerate(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		multiplayer.toggleFreerate(user);
	}
	
	@CommandAlias(values = {"randint"})
	@HelpMessage(desc = "Roll a number between 1 and x. If x is not given, uses 100 instead", usage = "/roll [x]")
	void cmd_roll(CommandData data, UserSession user) {
		int max = 100;
		if (data.getArgs() == null || data.getArgs().size() == 0) {
			try {
				max = Integer.parseInt(data.getArgs().get(0));
			} catch (Exception e) {}
		}
		if (max < 1) max = 1;
		final int r = new Random().nextInt(max) + 1;
		final String msg = String.format("%s rolled a %d out of %d", user.getUsername(), r, max);
		ChatMessageType msgType = ChatMessageType.fromInt(data.getMsgData().getMsgtype());
		switch (msgType) {
			default:
			case LOBBY: {
				multiplayer.systemMessageToGlobalChat(msg);
				break;
			}
			case ROOM: {
				responder.systemNoticeToLobby(user.getLobby(), msg);
				break;
			}
			case PRIVATE: {
				UserSession otherUser = sessions.getByUsername(data.getMsgData().getTab());
				if (otherUser != null) {
					responder.systemNoticeToUserInPrivate(user, msg, data.getMsgData().getTab());
					responder.systemNoticeToUserInPrivate(otherUser, msg, user.getUsername());
				}
				break;
			}
		}
	}
	
	@CommandAlias(values = {"ban"})
	@HelpMessage(desc = "Kick and ban a user from your room", usage = "/kick <user>", requiresOper = true)
	void cmd_kick(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		if (data.getArgs() == null || data.getArgs().size() == 0) {
			responder.systemNoticeToUserInContext(user, "Please provide the name to kick.", data.getMsgData().getMsgtype(), data.getMsgData().getTab());
			return;
		}
		
		multiplayer.banFromLobby(user, data.getArgs().get(0));
	}
	
	@HelpMessage(desc = "Unban a kicked/banned user from your room", usage = "/unban <user>", requiresOper = true)
	void cmd_unban(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		if (data.getArgs() == null || data.getArgs().size() == 0) {
			responder.systemNoticeToUserInContext(user, "Please provide the name to kick.", data.getMsgData().getMsgtype(), data.getMsgData().getTab());
			return;
		}
		
		multiplayer.unbanFromLobby(user, data.getArgs().get(0));
	}
	
	@CommandAlias(values = {"oper", "operator", "unop", "unoper", "deop", "deoper", "unoperator", "deoperator"})
	@HelpMessage(desc = "Give or remove operator (moderator) status to a member in your room", usage = "/op <user>", requiresOwner = true)
	void cmd_op(CommandData data, UserSession user) {
		if (user.getLobby() == null) {
			responder.systemNoticeToUserInGlobalChat(user, "You aren't in a lobby");
			return;
		}
		if (data.getArgs() == null || data.getArgs().size() == 0) {
			responder.systemNoticeToUserInContext(user, "Please provide a user name to give or take op status.", data.getMsgData().getMsgtype(), data.getMsgData().getTab());
			return;
		}
		
		multiplayer.toggleOperator(user, data.getArgs().get(0));
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
