package com.handicap.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientLimbs {
	private static final Map<UUID, Integer> LIMBS = new ConcurrentHashMap<>();

	private ClientLimbs() {
	}

	public static void set(UUID player, int limbs) {
		if (limbs == 0) {
			LIMBS.remove(player);
		} else {
			LIMBS.put(player, limbs);
		}
	}

	public static int get(UUID player) {
		return LIMBS.getOrDefault(player, 0);
	}

	public static void clear() {
		LIMBS.clear();
	}
}
