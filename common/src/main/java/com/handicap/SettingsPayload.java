package com.handicap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SettingsPayload(String key, int value) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SettingsPayload> TYPE = new CustomPacketPayload.Type<>(HandicapMod.id("settings"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SettingsPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, SettingsPayload::key,
		ByteBufCodecs.VAR_INT, SettingsPayload::value,
		SettingsPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
