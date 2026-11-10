package com.handicap.client;

import com.handicap.Handicap;

public final class ClientState {
	public static volatile long mask = 0L;
	public static volatile long mask2 = 0L;
	public static volatile int interval = 600;
	public static volatile int duration = 0;
	public static volatile long disabled = 0L;
	public static volatile long disabled2 = 0L;
	public static volatile java.util.function.Consumer<net.minecraft.network.protocol.common.custom.CustomPacketPayload> sender;
	public static volatile int flags = 0;
	public static volatile int timer = -1;
	public static volatile int maxHandicaps = 0;

	private ClientState() {
	}

	public static boolean has(Handicap d) {
		return ((d.high() ? mask2 : mask) & d.bit()) != 0;
	}

	public static boolean disabled(Handicap d) {
		return ((d.high() ? disabled2 : disabled) & d.bit()) != 0;
	}

	public static boolean permissionPlayer() {
		return (flags & 64) != 0;
	}

	public static boolean sync() {
		return (flags & 32) != 0;
	}

	public static void apply(com.handicap.SyncPayload payload) {
		mask = payload.mask();
		mask2 = payload.mask2();
		flags = payload.flags();
		timer = payload.timer();
		interval = payload.interval();
		maxHandicaps = payload.maxHandicaps();
		duration = payload.duration();
		disabled = payload.disabled();
		disabled2 = payload.disabled2();
	}

	public static boolean seizing() {
		return (flags & 1) != 0;
	}

	public static boolean asleep() {
		return (flags & 2) != 0;
	}

	public static boolean showDescriptions() {
		return (flags & 4) != 0;
	}

	public static boolean stacking() {
		return (flags & 8) != 0;
	}

	public static void reset() {
		mask = 0L;
		mask2 = 0L;
		disabled = 0L;
		disabled2 = 0L;
		flags = 0;
		timer = -1;
		maxHandicaps = 0;
	}
}
