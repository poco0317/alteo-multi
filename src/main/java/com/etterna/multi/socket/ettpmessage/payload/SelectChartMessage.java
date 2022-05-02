package com.etterna.multi.socket.ettpmessage.payload;

public class SelectChartMessage {
	private String title;
	private String subtitle;
	private String artist;
	private String filehash;
	private String chartkey;
	private String pack;
	private Double rate;
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
	public String getChartkey() {
		return chartkey;
	}
	public void setChartkey(String chartkey) {
		this.chartkey = chartkey;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
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
}
