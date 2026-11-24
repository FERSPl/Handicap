package com.handicap;

import com.handicap.client.ClientState;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class HandicapRules {
	public static final float SHORT_SCALE = 0.45f;
	public static volatile boolean clientJumpBlocked;

	private HandicapRules() {
	}

	public static UUID modifierId(ResourceLocation id) {
		return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
	}

	private static boolean has(Player p, Handicap d) {
		if (p.level() == null) {
			return false;
		}
		if (p.level().isClientSide()) {
			return p.isLocalPlayer() && ClientState.has(d);
		}
		HandicapState state = HandicapManager.state();
		return state != null && state.has(p.getUUID(), d);
	}

	public static boolean isShort(Player p) {
		return has(p, Handicap.SHORT);
	}

	public static float jumpFactor(LivingEntity entity) {
		if (!(entity instanceof Player p) || p.level() == null || !p.level().isClientSide() || !p.isLocalPlayer()) {
			return 1.0f;
		}
		if (clientJumpBlocked || ClientState.has(Handicap.PARAPLEGIA) || ClientState.asleep() || ClientState.seizing()) {
			return 0.0f;
		}
		return ClientState.has(Handicap.SHORT) ? SHORT_SCALE : 1.0f;
	}

	public static float breakFactor(Player p) {
		float factor = 1.0f;
		if (has(p, Handicap.ARTHRITIS)) {
			factor *= 0.5f;
		}
		if (has(p, Handicap.BROKEN_ARM)) {
			factor *= 0.35f;
		}
		return factor;
	}

	public static float fallFactor(LivingEntity entity) {
		return entity instanceof Player p && !p.level().isClientSide() && has(p, Handicap.BRITTLE_BONES) ? 2.0f : 1.0f;
	}
}
