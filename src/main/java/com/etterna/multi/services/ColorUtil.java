package com.etterna.multi.services;

import com.etterna.multi.data.state.UserSession;

public class ColorUtil {
	
	public static final String COLOR_ROOM_OWNER = "BBFFBB";
	public static final String COLOR_PLAYER = "AAFFFF";
	public static final String COLOR_ROOM_OP = "FFBBBB";
	public static final String COLOR_SYSTEM = "BBBBFF";
	public static final String COLOR_WHITE = "FFFFFF";
	
	public static String removeAllColor(String s) {
		return s.replaceAll("(\\|c[0-9A-Fa-f]{7}(\\s*))*(\\|c[0-9A-Fa-f]{7})", "");
	}
	
	public static String color(String s) {
		return "|c0"+s;
	}
	
	public static String colorize(String s, String color) {
		return color(color) + s + color(COLOR_WHITE);
	}
	
	public static String system(String s) {
		return colorize("System:", COLOR_SYSTEM) + " " + s;
	}
	
	public static String colorUser(UserSession user) {
		if (user.getLobby() != null) {
			if (user.getLobby().isOwner(user)) {
				return COLOR_ROOM_OWNER;
			}
			else if (user.getLobby().isOperator(user)) {
				return COLOR_ROOM_OP;
			}
			else {
				return COLOR_PLAYER;
			}
		}
		return COLOR_PLAYER;
	}
	

}
