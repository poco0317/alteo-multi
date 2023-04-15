package com.etterna.multi.services;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.amazonaws.util.json.Jackson;
import com.etterna.multi.data.state.Lobby;
import com.etterna.multi.data.state.UserSession;
import com.etterna.multi.socket.ettpmessage.ChatMessageType;
import com.etterna.multi.socket.ettpmessage.EttpMessageResponse;
import com.etterna.multi.socket.ettpmessage.client.payload.ChatMessage;

@Service
public class EttpResponseMessageService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(EttpResponseMessageService.class);

	private static final String CHAT_RESPONSE_TYPE = "chat";
	
	/**
	 * Send a particular data carrying message to a given session
	 */
	public <T> void respond(WebSocketSession session, String messageType, T ettpMessageResponse) {
		if (session == null || !session.isOpen()) return;
		try {
			EttpMessageResponse<T> response = new EttpMessageResponse<>();
			response.setPayload(ettpMessageResponse);
			response.setType(messageType);
			session.sendMessage(new TextMessage(Jackson.toJsonString(response)));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
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
		for (UserSession user : users) {
			respond(user.getSession(), messageType, ettpMessageResponse);
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
	
	/**
	 * Send a system notice in chat that only a specific user can see.
	 * Either send in DM or in a room. Leave last parameter null or blank to send in DM. 
	 */
	public void systemNoticeToUser(UserSession user, String message, String roomToSendTo) {
		if (user == null) return;
		String tab = "";
		if (roomToSendTo != null) {
			tab = roomToSendTo;
		}
		respond(user.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage(tab, ChatMessageType.PRIVATE, ColorUtil.system(message)));
	}
	
	/**
	 * Send a system notice to a user in the main lobby
	 */
	public void systemNoticeToUserInMainLobby(UserSession user, String message) {
		if (user == null) return;
		respond(user.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage("", ChatMessageType.LOBBY, ColorUtil.system(message)));
	}
	
	/**
	 * Send a system notice to everyone in a room
	 */
	public void systemNoticeToEntireLobby(Lobby lobby, String message) {
		if (lobby == null) return;
		respondToLobby(lobby, CHAT_RESPONSE_TYPE, makeChatMessage(lobby.getName(), ChatMessageType.ROOM, ColorUtil.system(message)));
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
	public void userChatToMainLobby(UserSession recipient, final String senderName, String message) {
		if (recipient == null || recipient.getUsername() == null || senderName == null) return;
		String msgLine = senderName + ": " + message;
		respond(recipient.getSession(), CHAT_RESPONSE_TYPE, makeChatMessage("", ChatMessageType.LOBBY, msgLine));
	}

}
