package com.etterna.multi.socket.ettpmessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.amazonaws.util.json.Jackson;
import com.etterna.multi.services.SessionService;
import com.etterna.multi.socket.ettpmessage.payload.ChatMessage;

/**
 * Base handler for all EttpMessages.
 * Responsible for dealing with and then responding to any given EttpMessage.
 * May be brought into another class as a dependency (all implementations of this should be Components)
 * The dependency can then be used for special invoke purposes such as the Hello message on connect instead of responding to a Hello input
 */
public abstract class EttpMessageHandler {
	
	protected static final Logger m_logger = LoggerFactory.getLogger(EttpMessageHandler.class);
	
	@Autowired
	protected SessionService sessions;
	
	public static <T> T readPayload(EttpMessage msg, Class<T> clz) {
		return Jackson.fromJsonString(Jackson.toJsonString(msg.getPayload()), clz);
	}
	
	public abstract void handle(WebSocketSession session, EttpMessage message);
	
	protected <T> void respond(WebSocketSession session, String messageType, T ettpMessageResponse) {
		try {
			EttpMessageResponse<T> response = new EttpMessageResponse<>();
			response.setPayload(ettpMessageResponse);
			response.setType(messageType);
			session.sendMessage(new TextMessage(Jackson.toJsonString(response)));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	protected void noticeToUser(WebSocketSession session, String message) {
		try {
			EttpMessageResponse<ChatMessage> response = new EttpMessageResponse<>();
			response.setType("chat");
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setTab("");
			chatMessage.setMsgtype(ChatMessageType.PRIVATE.num());
			chatMessage.setMsg(message);
			response.setPayload(chatMessage);
			session.sendMessage(new TextMessage(Jackson.toJsonString(response)));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	protected void noticeToRoom(WebSocketSession session, String message, String room) {
		try {
			EttpMessageResponse<ChatMessage> response = new EttpMessageResponse<>();
			response.setType("chat");
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setTab(room);
			chatMessage.setMsgtype(ChatMessageType.ROOM.num());
			chatMessage.setMsg(message);
			response.setPayload(chatMessage);
			session.sendMessage(new TextMessage(Jackson.toJsonString(response)));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

}
