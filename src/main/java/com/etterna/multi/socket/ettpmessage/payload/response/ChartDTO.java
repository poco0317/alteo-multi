package com.etterna.multi.socket.ettpmessage.payload.response;

public class ChartDTO {
	private String chartkey;
	private String title;
	private String subtitle;
	private String artist;
	private String difficulty;
	private Double meter;
	private String filehash;
	private Double rate;
	public String getChartkey() {
		return chartkey;
	}
	public void setChartkey(String chartkey) {
		this.chartkey = chartkey;
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
}