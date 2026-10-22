package com.handicap.fabric;

import com.handicap.HandicapCommands;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.SyncPayload;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

public class HandicapFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(SyncPayload.TYPE, SyncPayload.CODEC);

		HandicapMod.init(new HandicapMod.Platform() {
			@Override
			public boolean canSend(ServerPlayer player) {
				return ServerPlayNetworking.canSend(player, SyncPayload.TYPE);
			}

			@Override
			public void send(ServerPlayer player, SyncPayload payload) {
				ServerPlayNetworking.send(player, payload);
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(HandicapManager::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> HandicapManager.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> HandicapManager.onServerStopped());
		ServerTickEvents.END_SERVER_TICK.register(HandicapManager::onServerTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> HandicapManager.onPlayerJoin(handler.getPlayer()));
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> HandicapManager.allowChat(sender));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			HandicapCommands.register(dispatcher));
	}
}
