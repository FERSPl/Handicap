package com.handicap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncPayload(long mask, long mask2, int flags, int timer, int interval, int maxHandicaps, int duration,
		long disabled, long disabled2) implements HandicapPayload {
	public static final ResourceLocation ID = HandicapMod.id("sync");

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarLong(mask);
		buf.writeVarLong(mask2);
		buf.writeVarInt(flags);
		buf.writeVarInt(timer);
		buf.writeVarInt(interval);
		buf.writeVarInt(maxHandicaps);
		buf.writeVarInt(duration);
		buf.writeVarLong(disabled);
		buf.writeVarLong(disabled2);
	}

	public static SyncPayload read(FriendlyByteBuf buf) {
		return new SyncPayload(buf.readVarLong(), buf.readVarLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			buf.readVarInt(), buf.readVarInt(), buf.readVarLong(), buf.readVarLong());
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
