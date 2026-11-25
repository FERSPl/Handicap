package com.handicap.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientLimbs {
	private static final Map<UUID, Integer> LIMBS = new ConcurrentHashMap<>();
	public static volatile java.util.function.Consumer<UUID> onShortChange;

	private ClientLimbs() {
	}

	public static void set(UUID player, int limbs) {
		boolean wasShort = (get(player) & com.handicap.LimbsPayload.SHORT) != 0;
		if (limbs == 0) {
			LIMBS.remove(player);
		} else {
			LIMBS.put(player, limbs);
		}
		boolean isShort = (limbs & com.handicap.LimbsPayload.SHORT) != 0;
		if (wasShort != isShort && onShortChange != null) {
			onShortChange.accept(player);
		}
	}

	public static int get(UUID player) {
		return LIMBS.getOrDefault(player, 0);
	}

	public static void clear() {
		LIMBS.clear();
	}
}
