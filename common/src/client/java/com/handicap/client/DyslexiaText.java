package com.handicap.client;

import com.handicap.Handicap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class DyslexiaText {
	private static final Pattern WORD = Pattern.compile("(?<!\u00a7)\\p{L}{4,}");

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
		if (text == null || text.length() < 4 || !ClientState.has(Handicap.DYSLEXIA) || exempt()) {
			return text;
		}
		String cached = CACHE.get(text);
		if (cached == null) {
			cached = scramble(text);
			CACHE.put(text, cached);
		}
		return cached;
	}

	private static boolean exempt() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null) {
			return true;
		}
		Screen screen = mc.gui.screen();
		if (screen == null) {
			return false;
		}
		return !screen.getClass().getName().contains(".inventory.");
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
			for (int i = chars.length - 1; i > 1; i--) {
				int j = 1 + rnd.nextInt(i);
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
