package com.disabilitymod.client;

import com.disabilitymod.Disability;

/** État reçu du serveur (handicaps actifs et crise en cours). */
public final class ClientState {
	public static volatile int mask = 0;
	public static volatile int flags = 0;

	private ClientState() {
	}

	public static boolean has(Disability d) {
		return (mask & d.bit()) != 0;
	}

	public static boolean seizing() {
		return (flags & 1) != 0;
	}

	public static void reset() {
		mask = 0;
		flags = 0;
	}
}
