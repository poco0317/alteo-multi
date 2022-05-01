package com.etterna.multi.socket.ettpmessage;

/**
 * Represents the ETTP messages, where a websocket payload contains a payload
 * Utilized for JSON conversions
 */
public class EttpMessage {
	
	private Long id;
	private String type;
	private Object payload;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getPayload() {
		return payload;
	}
	public void setPayload(Object payload) {
		this.payload = payload;
	}
	@Override
	public String toString() {
		return "EttpMessage [id=" + id + ", type=" + type + ", payload=" + payload + "]";
	}

}
