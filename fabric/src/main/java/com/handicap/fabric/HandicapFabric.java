package com.handicap.fabric;

import com.handicap.HandicapCommands;
import com.handicap.HandicapContent;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;
import com.handicap.SettingsPayload;
import com.handicap.SyncPayload;
import com.handicap.WheelchairEntity;

import java.nio.file.Path;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class HandicapFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, HandicapContent.WHEELCHAIR_ENTITY_KEY, HandicapContent.createEntity());
		Registry.register(BuiltInRegistries.ITEM, HandicapContent.WHEELCHAIR_ITEM_KEY, HandicapContent.createItem());
		FabricDefaultAttributeRegistry.register(HandicapContent.wheelchair(), WheelchairEntity.createAttributes());
		CreativeModeTabEvents.modifyOutputEvent(HandicapContent.TOOLS_TAB).register(output ->
			output.accept(HandicapContent.wheelchairItem()));

		PayloadTypeRegistry.clientboundPlay().register(SyncPayload.TYPE, SyncPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(LimbsPayload.TYPE, LimbsPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SettingsPayload.TYPE, SettingsPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SettingsPayload.TYPE, (payload, context) ->
			HandicapManager.applySetting(context.player(), payload.key(), payload.value()));

		HandicapMod.init(new HandicapMod.Platform() {
			@Override
			public boolean canSend(ServerPlayer player) {
				return ServerPlayNetworking.canSend(player, SyncPayload.TYPE);
			}

			@Override
			public void send(ServerPlayer player, CustomPacketPayload payload) {
				ServerPlayNetworking.send(player, payload);
			}

			@Override
			public Path configDir() {
				return FabricLoader.getInstance().getConfigDir();
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
