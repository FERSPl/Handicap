package com.handicap;

import java.nio.file.Path;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-neutral entry point. Each loader module (fabric, neoforge) calls {@link #init(Platform)}
 * and then forwards its own events to {@link HandicapManager}.
 */
public final class HandicapMod {
	public static final String MOD_ID = "handicap";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** What the common code needs from the loader. */
	public interface Platform {
		/** True if the player's client has the mod (can receive {@link SyncPayload}). */
		boolean canSend(ServerPlayer player);

		void send(ServerPlayer player, CustomPacketPayload payload);

		Path configDir();
	}

	private static Platform platform;

	private HandicapMod() {
	}

	public static void init(Platform p) {
		platform = p;
		HandicapConfig.load(p.configDir());
		LOGGER.info("Handicap loaded.");
	}

	public static Platform platform() {
		return platform;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
