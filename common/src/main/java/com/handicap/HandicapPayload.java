package com.handicap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface HandicapPayload {
	ResourceLocation id();

	void write(FriendlyByteBuf buf);
}
