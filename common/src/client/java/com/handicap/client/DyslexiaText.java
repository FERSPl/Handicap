package com.handicap.client;

import com.handicap.Handicap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class DyslexiaText {
	private static final Pattern WORD = Pattern.compile("(?<![\\p{L}\\p{N}\u00a7])(?:\\p{L}{3,}|\\d+)(?![\\p{L}\\p{N}])");

	private static final Map<String, String> CACHE = Collections.synchronizedMap(
		new LinkedHashMap<String, String>(256, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
				return size() > 4096;
			}
		});

	private DyslexiaText() {
	}

	public static String apply(String text) {
		if (text == null || text.length() < 2 || !ClientState.has(Handicap.DYSLEXIA)) {
			return text;
		}
		String cached = CACHE.get(text);
		if (cached == null) {
			cached = scramble(text);
			CACHE.put(text, cached);
		}
		return cached;
	}

	private static String scramble(String text) {
		Matcher m = WORD.matcher(text);
		StringBuilder out = new StringBuilder();
		int last = 0;
		while (m.find()) {
			out.append(text, last, m.start());
			String word = m.group();
			char[] chars = word.toCharArray();
			Random rnd = new Random(word.hashCode());
			boolean number = word.chars().allMatch(Character::isDigit);
			for (int i = chars.length - 1; i > (number ? 0 : 1); i--) {
				int j = number ? rnd.nextInt(i + 1) : 1 + rnd.nextInt(i);
				char tmp = chars[i];
				chars[i] = chars[j];
				chars[j] = tmp;
			}
			out.append(chars);
			last = m.end();
		}
		out.append(text, last, text.length());
		return out.toString();
	}
}
