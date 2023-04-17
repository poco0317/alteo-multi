package com.etterna.multi.socket.ettpmessage;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketSession;

import com.etterna.multi.services.CommandService;
import com.etterna.multi.services.EttpResponseMessageService;
import com.etterna.multi.services.MultiplayerService;
import com.etterna.multi.services.SessionService;
import com.etterna.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base handler for all EttpMessages.
 * Responsible for dealing with and then responding to any given EttpMessage.
 * May be brought into another class as a dependency (all implementations of this should be Components)
 * The dependency can then be used for special invoke purposes such as the Hello message on connect instead of responding to a Hello input
 */
public abstract class EttpMessageHandler {
	
	protected static final Logger m_logger = LoggerFactory.getLogger(EttpMessageHandler.class);
	protected static final ObjectMapper mapper = Util.objectMapper();
	
	@Autowired
	protected SessionService sessions;
	
	@Autowired
	protected MultiplayerService multiplayer;
	
	@Autowired
	protected EttpResponseMessageService responder;
	
	@Autowired
	protected CommandService commands;
	
	public static <T> T readPayload(EttpMessage msg, Class<T> clz) throws IOException {
		return mapper.readerFor(clz).readValue(mapper.writer().writeValueAsString(msg.getPayload()));
	}
	
	public abstract void handle(WebSocketSession session, EttpMessage message) throws IOException;

}
