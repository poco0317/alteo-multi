package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReplayV2DataMessage {
	
	private Integer col;
	private Integer row;
	private Float offset;
	private Integer tapnote_type;

}
