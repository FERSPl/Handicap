package com.handicap;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record LimbsPayload(UUID player, int limbs) implements HandicapPayload {
	public static final int LEFT_ARM = 1;
	public static final int RIGHT_ARM = 2;
	public static final int LEFT_LEG = 4;
	public static final int RIGHT_LEG = 8;
	public static final int CAST_LEFT_ARM = 16;
	public static final int CAST_RIGHT_ARM = 32;
	public static final int SHORT = 64;

	public static final ResourceLocation ID = HandicapMod.id("limbs");

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUUID(player);
		buf.writeVarInt(limbs);
	}

	public static LimbsPayload read(FriendlyByteBuf buf) {
		return new LimbsPayload(buf.readUUID(), buf.readVarInt());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
