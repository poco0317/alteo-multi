package com.etterna.util;

public class WordFilter {
	
	private static final String[] words = new String[] {
			"nig",
			"fag",
	};
	
	private static final String[] whitelist = new String[] {
		"night",
	};
	
	public static boolean isFiltered(String str) {
		for (String x : words) {
			if (str.toLowerCase().contains(x)) {
				boolean failed = true;
				for (String wl : whitelist) {
					if (!str.replaceAll(wl, "").contains(x)) {
						failed = false;
					}
				}
				if (failed) {
					return failed;
				}
			}
		}
		return false;
	}

}
