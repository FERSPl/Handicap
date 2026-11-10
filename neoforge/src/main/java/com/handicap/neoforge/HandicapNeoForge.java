package com.handicap.neoforge;

import com.handicap.HandicapCommands;
import com.handicap.HandicapContent;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;
import com.handicap.SettingsPayload;
import com.handicap.SyncPayload;
import com.handicap.WheelchairEntity;
import com.handicap.client.ClientLimbs;
import com.handicap.client.ClientState;

import java.nio.file.Path;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(HandicapMod.MOD_ID)
public class HandicapNeoForge {
	public HandicapNeoForge(IEventBus modBus) {
		HandicapMod.init(new HandicapMod.Platform() {
			@Override
			public boolean canSend(ServerPlayer player) {
				return player.connection.hasChannel(SyncPayload.TYPE);
			}

			@Override
			public void send(ServerPlayer player, CustomPacketPayload payload) {
				PacketDistributor.sendToPlayer(player, payload);
			}

			@Override
			public Path configDir() {
				return FMLPaths.CONFIGDIR.get();
			}
		});

		modBus.addListener(this::registerContent);
		modBus.addListener(this::registerAttributes);
		modBus.addListener(this::addToCreativeTab);
		modBus.addListener(this::registerPayloads);

		NeoForge.EVENT_BUS.addListener(this::onServerStarted);
		NeoForge.EVENT_BUS.addListener(this::onServerStopping);
		NeoForge.EVENT_BUS.addListener(this::onServerStopped);
		NeoForge.EVENT_BUS.addListener(this::onServerTick);
		NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
		NeoForge.EVENT_BUS.addListener(this::onChat);
		NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
	}

	private void registerContent(RegisterEvent event) {
		event.register(Registries.ENTITY_TYPE, helper ->
			helper.register(HandicapContent.WHEELCHAIR_ENTITY_KEY, HandicapContent.createEntity()));
		event.register(Registries.ITEM, helper ->
			helper.register(HandicapContent.WHEELCHAIR_ITEM_KEY, HandicapContent.createItem()));
	}

	private void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HandicapContent.wheelchair(), WheelchairEntity.createAttributes().build());
	}

	private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == HandicapContent.TOOLS_TAB) {
			event.accept(HandicapContent.wheelchairItem());
		}
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		// Optional: the mod also works when the other side does not have it.
		var registrar = event.registrar("3").optional();
		registrar.playToClient(SyncPayload.TYPE, SyncPayload.CODEC, (payload, context) -> {
			ClientState.apply(payload);
		});
		registrar.playToServer(SettingsPayload.TYPE, SettingsPayload.CODEC, (payload, context) -> context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				HandicapManager.applySetting(player, payload.key(), payload.value());
			}
		}));
		registrar.playToClient(LimbsPayload.TYPE, LimbsPayload.CODEC, (payload, context) ->
			ClientLimbs.set(payload.player(), payload.limbs()));
	}

	private void onServerStarted(ServerStartedEvent event) {
		HandicapManager.onServerStarted(event.getServer());
	}

	private void onServerStopping(ServerStoppingEvent event) {
		HandicapManager.onServerStopping();
	}

	private void onServerStopped(ServerStoppedEvent event) {
		HandicapManager.onServerStopped();
	}

	private void onServerTick(ServerTickEvent.Post event) {
		HandicapManager.onServerTick(event.getServer());
	}

	private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			HandicapManager.onPlayerJoin(player);
		}
	}

	private void onChat(ServerChatEvent event) {
		if (!HandicapManager.allowChat(event.getPlayer())) {
			event.setCanceled(true);
		}
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		HandicapCommands.register(event.getDispatcher());
	}
}
