package com.etterna.multi.socket.ettpmessage;

import lombok.Getter;
import lombok.Setter;

/**
 * JSON class for the EttpMessage which can be read by the Etterna client
 */
@Getter @Setter
public class EttpMessageResponse<T> {
	private String type;
	private T payload;
}
