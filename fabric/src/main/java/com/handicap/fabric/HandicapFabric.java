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
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import com.handicap.HandicapPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class HandicapFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, HandicapContent.WHEELCHAIR_ENTITY_KEY, HandicapContent.createEntity());
		Registry.register(BuiltInRegistries.ITEM, HandicapContent.WHEELCHAIR_ITEM_KEY, HandicapContent.createItem());
		FabricDefaultAttributeRegistry.register(HandicapContent.wheelchair(), WheelchairEntity.createAttributes());
		ItemGroupEvents.modifyEntriesEvent(HandicapContent.TOOLS_TAB).register(entries ->
			entries.accept(HandicapContent.wheelchairItem()));

		ServerPlayNetworking.registerGlobalReceiver(SettingsPayload.ID, (server, player, handler, buf, responseSender) -> {
			SettingsPayload payload = SettingsPayload.read(buf);
			server.execute(() -> HandicapManager.applySetting(player, payload.key(), payload.value()));
		});

		HandicapMod.init(new HandicapMod.Platform() {
			@Override
			public boolean canSend(ServerPlayer player) {
				return ServerPlayNetworking.canSend(player, SyncPayload.ID);
			}

			@Override
			public void send(ServerPlayer player, HandicapPayload payload) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				payload.write(buf);
				ServerPlayNetworking.send(player, payload.id(), buf);
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
