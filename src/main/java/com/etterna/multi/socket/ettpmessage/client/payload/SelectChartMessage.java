package com.etterna.multi.socket.ettpmessage.client.payload;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SelectChartMessage {
	private String title;
	private String subtitle;
	private String artist;
	private String filehash;
	private String chartkey;
	private String pack;
	private Integer rate;
	private String difficulty;
	private Double meter;
	public SelectChartMessage() {}
	public SelectChartMessage(StartChartMessage msg) {
		title = msg.getTitle();
		subtitle = msg.getSubtitle();
		artist = msg.getArtist();
		filehash = msg.getFilehash();
		chartkey = msg.getChartkey();
		pack = msg.getPack();
		rate = msg.getRate();
		difficulty = msg.getDifficulty();
		meter = msg.getMeter();

	}
}
