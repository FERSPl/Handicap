package com.handicap.client;

import com.handicap.Handicap;

public final class ClientState {
	public static volatile long mask = 0L;
	public static volatile int flags = 0;

	private ClientState() {
	}

	public static boolean has(Handicap d) {
		return (mask & d.bit()) != 0;
	}

	public static boolean seizing() {
		return (flags & 1) != 0;
	}

	public static boolean asleep() {
		return (flags & 2) != 0;
	}

	public static void reset() {
		mask = 0L;
		flags = 0;
	}
}
