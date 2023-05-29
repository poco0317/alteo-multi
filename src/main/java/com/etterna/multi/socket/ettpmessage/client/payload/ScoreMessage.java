package com.etterna.multi.socket.ettpmessage.client.payload;

import java.util.List;

import com.etterna.multi.data.LobbyScore;
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
	
	public LobbyScore toLobbyScore() {
		LobbyScore score = new LobbyScore();
		
		score.setSsrNorm((int)Math.round(1000000.0 * di(this.getSsr_norm())));
		
		score.setScoreKey(this.getScorekey());
		score.setChartKey(this.getChartkey());
		score.setMusicRate(di(dd(this.getRate()) * 100.0));
		score.setMaxCombo(this.getMax_combo());
		score.setEtternaValid(this.getValid());
		score.setModString(this.getMods());
		score.setMissCount(this.getMiss());
		score.setBadCount(this.getBad());
		score.setGoodCount(this.getGood());
		score.setGreatCount(this.getGreat());
		score.setPerfCount(this.getPerfect());
		score.setMarvCount(this.getMarv());
		score.setHitMineCount(this.getHitmine());
		score.setHeldCount(this.getHeld());
		score.setNgCount(this.getNg());
		score.setLetgoCount(this.getLetgo());
		score.setDateStr(this.getDatetime());
		score.setNegBpm(ib(this.getNegsolo()));
		score.setNoCC(ib(this.getNocc()));
		score.setCalcVersion(this.getCalc_version());
		score.setWifeVersion(this.getWifever());
		score.setTopScore(this.getTopscore());
		score.setBrittleKey(this.getHash());
		score.setGuid(this.getUuid());
		score.setWifePercent(this.getScore());
		score.setJudgeScale(0.0);
		score.setGrade(null);
		score.setWifeGrade(null);
		score.setSs1(this.getOverall());
		score.setSs2(this.getStream());
		score.setSs3(this.getJumpstream());
		score.setSs4(this.getHandstream());
		score.setSs5(this.getStamina());
		score.setSs6(this.getJackSpeed());
		score.setSs7(this.getChordjack());
		score.setSs8(this.getTechnical());
		
		return score;
	}
	
	private static Integer di(Double d) {
		if (d == null) {
			return 0;
		}
		return d.intValue();
	}
	
	private static Double dd(Double d) {
		if (d == null) {
			return 0.0;
		}
		return d;
	}
	
	private static Boolean ib(Integer i) {
		if (i == null) {
			return false;
		}
		return i == 1;
	}

}
