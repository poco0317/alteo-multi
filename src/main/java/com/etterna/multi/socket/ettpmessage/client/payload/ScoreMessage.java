package com.etterna.multi.socket.ettpmessage.client.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScoreMessage {
	
	private String scorekey;
	private Double ssr_norm;
	private Integer max_combo;
	private Integer valid;
	private String mods;
	private Double score; // ssr norm again
	private Integer wifever;
	private String chartkey;
	private Double rate;
	private String options; // mods again
	private Integer negsolo;
	private Integer nocc;
	private Integer calc_version;
	private Integer topscore;
	private String uuid;
	private String hash;
	private String datetime;
	
	private Integer miss;
	private Integer bad;
	private Integer good;
	private Integer great;
	private Integer perfect;
	private Integer marv;
	
	@JsonProperty("Notes")
	private Integer Notes;
	@JsonProperty("TapsAndHolds")
	private Integer TapsAndHolds;
	@JsonProperty("Jumps")
	private Integer Jumps;
	@JsonProperty("Holds")
	private Integer Holds;
	@JsonProperty("Mines")
	private Integer Mines;
	@JsonProperty("Hands")
	private Integer Hands;
	@JsonProperty("Rolls")
	private Integer Rolls;
	@JsonProperty("Lifts")
	private Integer Lifts;
	@JsonProperty("Fakes")
	private Integer Fakes;
	
	@JsonProperty("Overall")
	private Double Overall;
	@JsonProperty("Stream")
	private Double Stream;
	@JsonProperty("Jumpstream")
	private Double Jumpstream;
	@JsonProperty("Handstream")
	private Double Handstream;
	@JsonProperty("Stamina")
	private Double Stamina;
	@JsonProperty("JackSpeed")
	private Double JackSpeed;
	@JsonProperty("Chordjack")
	private Double Chordjack;
	@JsonProperty("Technical")
	private Double Technical;
	
	private Integer hitmine;
	private Integer held;
	private Integer letgo;
	private Integer ng;

	private Replay replay;
	
	@Getter @Setter
	public class Replay {
		private List<Integer> noterows;
		private List<Double> offsets;
		private List<Integer> tracks;
		private List<Integer> notetypes;
	}

}
