package com.handicap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SettingsPayload(String key, int value) implements HandicapPayload {
	public static final ResourceLocation ID = HandicapMod.id("settings");

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(key);
		buf.writeVarInt(value);
	}

	public static SettingsPayload read(FriendlyByteBuf buf) {
		return new SettingsPayload(buf.readUtf(), buf.readVarInt());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
