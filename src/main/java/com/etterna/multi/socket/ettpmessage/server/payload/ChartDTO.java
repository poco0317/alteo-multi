package com.etterna.multi.socket.ettpmessage.server.payload;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChartDTO {
	private String chartkey;
	private String title;
	private String subtitle;
	private String artist;
	private String difficulty;
	private Double meter;
	private String filehash;
	private Integer rate;
	
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