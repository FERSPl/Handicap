package com.handicap;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LimbsPayload(UUID player, int limbs) implements CustomPacketPayload {
	public static final int LEFT_ARM = 1;
	public static final int RIGHT_ARM = 2;
	public static final int LEFT_LEG = 4;
	public static final int RIGHT_LEG = 8;
	public static final int CAST_LEFT_ARM = 16;
	public static final int CAST_RIGHT_ARM = 32;

	public static final CustomPacketPayload.Type<LimbsPayload> TYPE = new CustomPacketPayload.Type<>(HandicapMod.id("limbs"));

	public static final StreamCodec<RegistryFriendlyByteBuf, LimbsPayload> CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC, LimbsPayload::player,
		ByteBufCodecs.VAR_INT, LimbsPayload::limbs,
		LimbsPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
