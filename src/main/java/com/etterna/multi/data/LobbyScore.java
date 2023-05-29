package com.etterna.multi.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.etterna.multi.socket.ettpmessage.client.payload.ScoreMessage;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "multi_lobby_scores")
@Getter @Setter
public class LobbyScore {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lobby")
	private GameLobby lobby;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user")
	private UserLogin user;
	
	@Column(name = "score_key")
	private String scoreKey;
	@Column(name = "chart_key")
	private String chartKey;
	
	@Column(name = "ssr_norm")
	private Integer ssrNorm;
	@Column(name = "music_rate")
	private Integer musicRate;
	@Column(name = "max_combo")
	private Integer maxCombo;
	@Column(name = "valid")
	private Integer etternaValid;
	@Column(name = "mods")
	private String modString;
	
	@Column(name = "miss_cnt")
	private Integer missCount;
	@Column(name = "bad_cnt")
	private Integer badCount;
	@Column(name = "good_cnt")
	private Integer goodCount;
	@Column(name = "great_cnt")
	private Integer greatCount;
	@Column(name = "perf_cnt")
	private Integer perfCount;
	@Column(name = "marv_cnt")
	private Integer marvCount;
	@Column(name = "hitmine_cnt")
	private Integer hitMineCount;
	@Column(name = "held_cnt")
	private Integer heldCount;
	@Column(name = "ng_cnt")
	private Integer ngCount;
	@Column(name = "letgo_cnt")
	private Integer letgoCount;
	@Column(name = "date_str")
	private String dateStr;
	
	@Column(name = "negbpm")
	private Boolean negBpm;
	@Column(name = "nocc")
	private Boolean noCC;
	
	@Column(name = "calc_vers")
	private Integer calcVersion = 0;
	@Column(name = "wife_vers")
	private Integer wifeVersion;
	@Column(name = "top_score")
	private Integer topScore;
	@Column(name = "brittle_key")
	private String brittleKey;
	@Column(name = "machine_guid")
	private String guid;
	
	@Column(name = "wife_perc")
	private Double wifePercent;
	@Column(name = "wife_pts")
	private Double wifePoints;
	@Column(name = "judge")
	private Double judgeScale;
	@Column(name = "grade")
	private String grade;
	@Column(name = "wife_grade")
	private String wifeGrade;
	
	@Column(name = "ss1") // overall
	private Double ss1;
	@Column(name = "ss2") // stream
	private Double ss2;
	@Column(name = "ss3") // jumpstream
	private Double ss3;
	@Column(name = "ss4") // handstream
	private Double ss4;
	@Column(name = "ss5") // stamina
	private Double ss5;
	@Column(name = "ss6") // jacks
	private Double ss6;
	@Column(name = "ss7") // cj
	private Double ss7;
	@Column(name = "ss8") // tech
	private Double ss8;
	
	public static LobbyScore fromScoreMessage(ScoreMessage msg) {
		LobbyScore score = new LobbyScore();
		
		score.setSsrNorm((int)Math.round(1000000.0 * di(msg.getSsr_norm())));
		
		score.setScoreKey(msg.getScorekey());
		score.setChartKey(msg.getChartkey());
		score.setMusicRate(di(dd(msg.getRate()) * 100.0));
		score.setMaxCombo(msg.getMax_combo());
		score.setEtternaValid(msg.getValid());
		score.setModString(msg.getMods());
		score.setMissCount(msg.getMiss());
		score.setBadCount(msg.getBad());
		score.setGoodCount(msg.getGood());
		score.setGreatCount(msg.getGreat());
		score.setPerfCount(msg.getPerfect());
		score.setMarvCount(msg.getMarv());
		score.setHitMineCount(msg.getHitmine());
		score.setHeldCount(msg.getHeld());
		score.setNgCount(msg.getNg());
		score.setLetgoCount(msg.getLetgo());
		score.setDateStr(msg.getDatetime());
		score.setNegBpm(ib(msg.getNegsolo()));
		score.setNoCC(ib(msg.getNocc()));
		score.setCalcVersion(msg.getCalc_version());
		score.setWifeVersion(msg.getWifever());
		score.setTopScore(msg.getTopscore());
		score.setBrittleKey(msg.getHash());
		score.setGuid(msg.getUuid());
		score.setWifePercent(msg.getScore());
		score.setJudgeScale(0.0);
		score.setGrade(null);
		score.setWifeGrade(null);
		score.setSs1(msg.getOverall());
		score.setSs2(msg.getStream());
		score.setSs3(msg.getJumpstream());
		score.setSs4(msg.getHandstream());
		score.setSs5(msg.getStamina());
		score.setSs6(msg.getJackSpeed());
		score.setSs7(msg.getChordjack());
		score.setSs8(msg.getTechnical());
		
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
