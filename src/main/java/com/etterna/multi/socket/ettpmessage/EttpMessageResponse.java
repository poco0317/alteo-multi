package com.etterna.multi.socket.ettpmessage;

/**
 * JSON class for the EttpMessage which can be read by the Etterna client
 */
public class EttpMessageResponse<T> {
	private String type;
	private T payload;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public T getPayload() {
		return payload;
	}
	public void setPayload(T payload) {
		this.payload = payload;
	}
}
