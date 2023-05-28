package com.etterna.multi.socket.ettpmessage.client.payload;

import java.util.List;

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
	
	private Integer Notes;
	private Integer TapsAndHolds;
	private Integer Jumps;
	private Integer Holds;
	private Integer Mines;
	private Integer Hands;
	private Integer Rolls;
	private Integer Lifts;
	private Integer Fakes;
	
	private Double Overall;
	private Double Stream;
	private Double Jumpstream;
	private Double Handstream;
	private Double Stamina;
	private Double JackSpeed;
	private Double Chordjack;
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
