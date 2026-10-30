package com.handicap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncPayload(long mask, int flags, int timer) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncPayload> TYPE = new CustomPacketPayload.Type<>(HandicapMod.id("sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG, SyncPayload::mask,
		ByteBufCodecs.VAR_INT, SyncPayload::flags,
		ByteBufCodecs.VAR_INT, SyncPayload::timer,
		SyncPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
