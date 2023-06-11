package com.etterna.multi.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.etterna.multi.services.MultiplayerService;
import com.etterna.multi.services.SessionService;
import com.etterna.multi.socket.ettpmessage.EttpMessage;
import com.etterna.multi.socket.ettpmessage.EttpMessageHandler;
import com.etterna.multi.socket.ettpmessage.EttpMessageType;
import com.etterna.multi.socket.ettpmessage.client.handler.HelloMessageHandler;
import com.etterna.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Scope("prototype")
public class SocketTextHandler extends TextWebSocketHandler {
	
	private static final Logger m_logger = LoggerFactory.getLogger(SocketTextHandler.class);
	
	private static final ObjectMapper mapper = Util.objectMapper();
	
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private MultiplayerService multiplayer;
	
	@Autowired
	private SessionService sessions;
	
	@Autowired
	private HelloMessageHandler hello;
	
	// set true to only log message types
	private static boolean SIMPLE_LOG = true;
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		
		EttpMessage ettpMessage;
		try {
			ettpMessage = mapper.readerFor(EttpMessage.class).readValue(message.getPayload());
		} catch (JsonProcessingException e) {
			m_logger.error("Failed to process message - "+e.getMessage(), e);
			multiplayer.killSession(session);
			return;
		}
		
		if (ettpMessage == null) {
			m_logger.warn("Failed to parse incoming message: {}", message.getPayload());
			return;
		}
		
		if ((message.getPayload() != null && message.getPayload().contains("pass")) || SIMPLE_LOG) {
			m_logger.debug("Got message type: {}", ettpMessage.getType());
		} else {
			m_logger.debug("Got message: {}", message.getPayload());
		}
		
		try {
			Class<? extends EttpMessageHandler> handlerClass = EttpMessageType.valueOf(ettpMessage.getType().toUpperCase()).getLinkedClass();
			try {
				m_logger.debug("Handling incoming message type: {}", ettpMessage.getType());
				multiplayer.pingSession(session);
				ctx.getBean(handlerClass).handle(session, ettpMessage);
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
				multiplayer.killSession(session);
				return;
			}
		} catch (Exception e) {
			m_logger.warn("Unknown incoming message type: {}", ettpMessage.getType());
			m_logger.error(e.getMessage(), e);
			return;
		}
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exc) {
		m_logger.error("Transport error occurred - closing session: "+exc.getMessage(), exc);
		multiplayer.killSession(session);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		m_logger.info("Session closed - status {} - {}", status.getCode(), status.getReason());
		multiplayer.killSession(session);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		m_logger.info("New session received - {}", session.getId());
		sessions.register(session);
		hello.hello(session);
	}

}
