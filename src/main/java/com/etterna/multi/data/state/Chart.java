package com.etterna.multi.data.state;

import com.etterna.multi.socket.ettpmessage.client.payload.SelectChartMessage;
import com.etterna.multi.socket.ettpmessage.client.payload.StartChartMessage;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Chart {
	
	private String title;
	private String subtitle;
	private String artist;
	private String filehash;
	private String pickedBy;
	private String difficulty;
	private Double meter;
	private String chartkey;
	private Integer rate;
	
	private Integer songoffset;
	private Integer globaloffset;
	
	public Chart() {}
	public Chart(SelectChartMessage msg) {
		title = msg.getTitle();
		subtitle = msg.getSubtitle();
		artist = msg.getArtist();
		filehash = msg.getFilehash();
		difficulty = msg.getDifficulty();
		meter = msg.getMeter();
		chartkey = msg.getChartkey();
		rate = msg.getRate();
		songoffset = msg.getSongoffset();
		globaloffset = msg.getGlobaloffset();
	}
	public Chart(StartChartMessage msg) {
		title = msg.getTitle();
		subtitle = msg.getSubtitle();
		artist = msg.getArtist();
		filehash = msg.getFilehash();
		difficulty = msg.getDifficulty();
		meter = msg.getMeter();
		chartkey = msg.getChartkey();
		rate = msg.getRate();
		songoffset = msg.getSongoffset();
		globaloffset = msg.getGlobaloffset();
	}
	

}
