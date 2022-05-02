package com.etterna.multi.socket.ettpmessage.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.services.ColorUtil;
import com.etterna.multi.socket.ettpmessage.ChatMessageType;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.payload.ChatMessage;

@Component
public class ChatMessageHandler extends EttpMessageHandler {
	
	@Override
	public void handle(WebSocketSession session, EttpMessage message) {
		ChatMessage msg = readPayload(message, ChatMessage.class);
		
		UserSession user = sessions.getUserSession(session);
		if (user == null || user.getUsername() == null) {
			return;
		}
		
		// remove newlines and stepmania newlines
		msg.setMsg(msg.getMsg().replaceAll("\n|::", ""));
		
		// a command
		if (msg.getMsg().startsWith("/")) {
			String[] params = msg.getMsg().split(" ");
			if (params.length == 0) {
				return;
			}
			String command = params[0].substring(1);
			String[] args = new String[params.length-1];
			for (int i = 1; i < params.length; i++) {
				args[i-1] = params[i];
			}
			
			// successful command usage if true
			if (commands.execute(session, msg, command, args)) {
				return;
			}
		}
		
		if (msg.getMsgtype() >= ChatMessageType.values().length) {
			// ???
			m_logger.warn("Unexpected message type {}", msg.getMsgtype());
			return;
		}
		
		ChatMessageType msgType = ChatMessageType.values()[msg.getMsgtype()];
		switch (msgType) {
			case LOBBY: {
				sessions.messageAllSessions(user, msg.getMsg());
				break;
			}
			case ROOM: {
				Lobby lobby = user.getLobby();
				if (lobby == null || !lobby.getName().equals(msg.getTab())) {
					responder.chatMessageToRoom(session, ColorUtil.system("You're not in the room '"+msg.getTab()+"'"), msg.getTab());
					return;
				}
				String coloredMsg = ColorUtil.colorize(user.getUsername(), ColorUtil.colorUser(user)) + ": " + msg.getMsg();
				for (UserSession u : lobby.getPlayers()) {
					responder.chatMessageToRoom(u.getSession(), coloredMsg, msg.getTab());
				}
				break;
			}
			case PRIVATE: {
				sessions.privateMessage(user, msg.getTab(), msg.getMsg());
				break;
			}
			default:
				m_logger.warn("Programmer error - unimplemented msgtype {}", msgType.name());
		}
		
	}

}
