package com.etterna.multi.data.state;

import com.etterna.multi.socket.ettpmessage.payload.SelectChartMessage;
import com.etterna.multi.socket.ettpmessage.payload.StartChartMessage;

public class Chart {
	
	private String title;
	private String subtitle;
	private String artist;
	private String filehash;
	private String pickedBy;
	private String difficulty;
	private Double meter;
	private String chartkey;
	private Double rate;
	
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
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getFilehash() {
		return filehash;
	}
	public void setFilehash(String filehash) {
		this.filehash = filehash;
	}
	public String getPickedBy() {
		return pickedBy;
	}
	public void setPickedBy(String pickedBy) {
		this.pickedBy = pickedBy;
	}
	public String getDifficulty() {
		return difficulty;
	}
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	public Double getMeter() {
		return meter;
	}
	public void setMeter(Double meter) {
		this.meter = meter;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
	public String getChartkey() {
		return chartkey;
	}
	public void setChartkey(String chartkey) {
		this.chartkey = chartkey;
	}

}
