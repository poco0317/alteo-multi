package com.etterna.multi.socket.ettpmessage;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the ETTP messages, where a websocket payload contains a payload
 * Utilized for JSON conversions
 */
@Getter @Setter
public class EttpMessage {
	
	private Long id;
	private String type;
	private Object payload;
	
	@Override
	public String toString() {
		return "EttpMessage [id=" + id + ", type=" + type + ", payload=" + payload + "]";
	}

}
