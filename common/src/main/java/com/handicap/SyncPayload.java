package com.handicap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncPayload(long mask, long mask2, int flags, int timer, int interval, int maxHandicaps, int duration,
		long disabled, long disabled2) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SyncPayload> TYPE = new CustomPacketPayload.Type<>(HandicapMod.id("sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncPayload> CODEC = CustomPacketPayload.codec(SyncPayload::write, SyncPayload::read);

	private void write(RegistryFriendlyByteBuf buf) {
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

	private static SyncPayload read(RegistryFriendlyByteBuf buf) {
		return new SyncPayload(buf.readVarLong(), buf.readVarLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			buf.readVarInt(), buf.readVarInt(), buf.readVarLong(), buf.readVarLong());
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
