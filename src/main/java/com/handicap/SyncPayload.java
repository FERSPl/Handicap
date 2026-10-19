package com.handicap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Envoyé du serveur au client : handicaps actifs (bits) et états temporaires (bit 0 = crise d'épilepsie). */
public record SyncPayload(int mask, int flags) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncPayload> TYPE = new CustomPacketPayload.Type<>(HandicapMod.id("sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SyncPayload::mask,
		ByteBufCodecs.VAR_INT, SyncPayload::flags,
		SyncPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
