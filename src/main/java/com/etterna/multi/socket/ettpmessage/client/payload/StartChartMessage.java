package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StartChartMessage {

	private String title;
	private String subtitle;
	private String artist;
	private String filehash;
	private String chartkey;
	private String pack;
	private Integer rate;
	private String difficulty;
	private Double meter;
	
}
