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
	
	/**
	 * Apply color formatting to a substring
	 */
	public static String colorize(String s, String color) {
		return color(color) + s + color(COLOR_WHITE);
	}
	
	/**
	 * Generate a system message using the system color
	 */
	public static String system(String s) {
		return colorize("System:", COLOR_SYSTEM) + " " + s;
	}
	
	/**
	 * Determine the color for a user based on their lobby
	 */
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
	
	/**
	 * Return a full message line for a given user and message, colorizing the username
	 */
	public static String colorUserMessage(UserSession user, String message) {
		return colorize(user.getUsername(), colorUser(user)) + ": " + message;
	}

}
