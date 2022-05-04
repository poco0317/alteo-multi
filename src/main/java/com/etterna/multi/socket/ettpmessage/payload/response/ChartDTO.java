package com.etterna.multi.socket.ettpmessage.payload.response;

import java.util.Objects;

public class ChartDTO {
	private String chartkey;
	private String title;
	private String subtitle;
	private String artist;
	private String difficulty;
	private Double meter;
	private String filehash;
	private Integer rate;
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
	public Integer getRate() {
		return rate;
	}
	public void setRate(Integer rate) {
		this.rate = rate;
	}
	@Override
	public int hashCode() {
		return Objects.hash(artist, chartkey, difficulty, filehash, meter, rate, subtitle, title);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChartDTO other = (ChartDTO) obj;
		return Objects.equals(artist, other.artist) && Objects.equals(chartkey, other.chartkey)
				&& Objects.equals(difficulty, other.difficulty) && Objects.equals(filehash, other.filehash)
				&& Objects.equals(meter, other.meter) && Objects.equals(rate, other.rate)
				&& Objects.equals(subtitle, other.subtitle) && Objects.equals(title, other.title);
	}
}