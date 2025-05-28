package com.etterna.multi.services;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.ChatMessageType;
import com.etterna.multi.socket.ettpmessage.EttpMessageResponse;
import com.etterna.multi.socket.ettpmessage.client.payload.ChatMessage;
import com.etterna.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EttpResponseMessageService {
	
	@Autowired
	private LobbyAuditingDispatch auditDispatch;
	
	private static final String CHAT_RESPONSE_TYPE = "chat";
	
	private static final ObjectMapper mapper = Util.objectMapper();
	
	/**
	 * Send a particular data carrying message to a given session
	 */
	public <T> void respond(WebSocketSession session, String messageType, T ettpMessageResponse) {
		if (session == null || !session.isOpen()) return;
		synchronized (session) {
			try {
				EttpMessageResponse<T> response = new EttpMessageResponse<>();
				response.setPayload(ettpMessageResponse);
				response.setType(messageType);
				m_logger.info("Sending messageType {} to {}", messageType, session.getRemoteAddress().toString());
				session.sendMessage(new TextMessage(mapper.writerFor(response.getClass()).writeValueAsString(response)));
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Send a particular data carrying message to a given user session
	 */
	public <T> void respond(UserSession session, String messageType, T ettpMessageResponse) {
		if (session == null) return;
		respond(session.getSession(), messageType, ettpMessageResponse);
	}
	
	/**
	 * Send a particular data carrying message to all players in a Lobby
	 */
	public <T> void respondToLobby(Lobby lobby, String messageType, T ettpMessageResponse) {
		if (lobby == null) return;
		respondToUsers(lobby.getPlayers(), messageType, ettpMessageResponse);
	}
	
	/**
	 * Send a particular data carrying message to a list of players
	 */
	public <T> void respondToUsers(Collection<UserSession> users, String messageType, T ettpMessageResponse) {
		if (users == null) return;
		Iterator<UserSession> it = users.iterator();
		while (it.hasNext()) {
			respond(it.next(), messageType, ettpMessageResponse);
		}
	}
	
	/**
	 * This central method is responsible for creating the message which gets sent to a session to represent a chat message.
	 * Modifying its output changes all chat messages users see.
	 */
	private ChatMessage makeChatMessage(String tab, ChatMessageType msgtype, String message) {
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setTab(tab);
		chatMessage.setMsgtype(msgtype.num());
		chatMessage.setMsg(" "+message);
		return chatMessage;
	}
	
	public void systemNoticeToUserInContext(UserSession user, String message, int msgType, String room) {
		ChatMessageType chatMessageType = ChatMessageType.fromInt(msgType);
		switch (chatMessageType) {
			default:
			case LOBBY:
				systemNoticeToUserInGlobalChat(user, message);
				break;
			case ROOM:
				systemNoticeToUserInRoom(user, message, room);
				break;
			case PRIVATE:
				systemNoticeToUserInPrivate(user, message, room);
				break;
		}
	}
	
	/**
	 * Send a system notice in chat that only a specific user can see.
	 * Creates a DM to send the message. Leaving the last parameter blank creates the System tab. 
	 */
	public void systemNoticeToUserInPrivate(UserSession user, String message, String roomToSendTo) {
		if (user == null) return;
		String tab = "";
		if (roomToSendTo != null) {
			tab = roomToSendTo;
		}
		respond(user.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage(tab, ChatMessageType.PRIVATE, ColorUtil.system(message)));
	}
	
	/**
	 * Send a system notice in chat that only a specific user can see.
	 * Meant specifically for a room instead of a DM.
	 */
	public void systemNoticeToUserInRoom(UserSession user, String message, String roomToSendTo) {
		if (user == null) return;
		String tab = "";
		if (roomToSendTo != null) {
			tab = roomToSendTo;
		}
		respond(user.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage(tab, ChatMessageType.ROOM, ColorUtil.system(message)));
	}
	
	/**
	 * Send a system notice to a user in the main lobby
	 */
	public void systemNoticeToUserInGlobalChat(UserSession user, String message) {
		if (user == null) return;
		respond(user.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage("", ChatMessageType.LOBBY, ColorUtil.system(message)));
	}
	
	/**
	 * Send a system notice to everyone in a room
	 */
	public void systemNoticeToLobby(Lobby lobby, String message) {
		if (lobby == null) return;
		respondToLobby(lobby, CHAT_RESPONSE_TYPE, makeChatMessage(lobby.getName(), ChatMessageType.ROOM, ColorUtil.system(message)));
		auditDispatch.roomMessage(lobby, "SYSTEM", message);
	}
	
	/**
	 * Handle a user sending a regular chat message to a room
	 */
	public void userChatToLobby(UserSession sender, String message) {
		if (sender == null || sender.getUsername() == null) return;
		Lobby lobby = sender.getLobby();
		if (lobby == null) return;
		String color = ColorUtil.colorUser(sender);
		String coloredMessage = ColorUtil.colorize(sender.getUsername(), color) + ": " + message;
		respondToLobby(lobby, CHAT_RESPONSE_TYPE, makeChatMessage(lobby.getName(), ChatMessageType.ROOM, coloredMessage));
		auditDispatch.roomMessage(lobby, sender.getUsername(), message);
	}
	
	/**
	 * Handle private messaging between users
	 */
	public void userChatPrivatelyToUser(UserSession sender, UserSession recipient, String message) {
		if (sender == null || recipient == null || sender.getUsername() == null || recipient.getUsername() == null) return;
		String msgLine = sender.getUsername() + ": " + message;
		respond(sender.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage(recipient.getUsername(), ChatMessageType.PRIVATE, msgLine));
		respond(recipient.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage(sender.getUsername(), ChatMessageType.PRIVATE, msgLine));
	}
	
	/**
	 * Send a chat message in the main lobby to a user
	 * This should be called via SessionService.chatToMainLobby
	 * Invoking this directly just sends a user their own message.
	 */
	public void userChatToGlobalChat(UserSession recipient, final String senderName, String message) {
		if (recipient == null || recipient.getUsername() == null || senderName == null) return;
		String msgLine = senderName + ": " + message;
		respond(recipient.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage("", ChatMessageType.LOBBY, msgLine));
	}

}
