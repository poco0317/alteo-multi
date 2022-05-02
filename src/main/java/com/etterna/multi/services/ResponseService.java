package com.etterna.multi.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.amazonaws.util.json.Jackson;
import com.etterna.multi.socket.ettpmessage.ChatMessageType;
import com.etterna.multi.socket.ettpmessage.EttpMessageResponse;
import com.etterna.multi.socket.ettpmessage.payload.ChatMessage;

@Service
public class ResponseService {
	
	private static final Logger m_logger = LoggerFactory.getLogger(ResponseService.class);
	
	
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
	
	private TextMessage makeChatMessage(String tab, ChatMessageType msgtype, String message) {
		EttpMessageResponse<ChatMessage> response = new EttpMessageResponse<>();
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setTab(tab);
		chatMessage.setMsgtype(msgtype.num());
		chatMessage.setMsg(message);
		response.setPayload(chatMessage);
		response.setType("chat");
		return new TextMessage(Jackson.toJsonString(response));
	}
	
	public void chatMessageToUser(WebSocketSession session, String message) {
		if (session == null || !session.isOpen()) return;
		try {
			session.sendMessage(makeChatMessage("", ChatMessageType.PRIVATE, message));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	public void chatMessageToRoom(WebSocketSession session, String message, String room) {
		if (session == null || !session.isOpen()) return;
		try {
			session.sendMessage(makeChatMessage(room, ChatMessageType.ROOM, message));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}
	
	public void chatMessageToLobby(WebSocketSession session, String message) {
		if (session == null || !session.isOpen()) return;
		try {
			session.sendMessage(makeChatMessage("", ChatMessageType.LOBBY, message));
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
	}

}
