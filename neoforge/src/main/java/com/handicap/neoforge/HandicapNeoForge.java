package com.handicap.neoforge;

import com.handicap.HandicapCommands;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.SyncPayload;
import com.handicap.client.ClientState;

import net.minecraft.server.level.ServerPlayer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
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
			public void send(ServerPlayer player, SyncPayload payload) {
				PacketDistributor.sendToPlayer(player, payload);
			}
		});

		modBus.addListener(this::registerPayloads);

		NeoForge.EVENT_BUS.addListener(this::onServerStarted);
		NeoForge.EVENT_BUS.addListener(this::onServerStopping);
		NeoForge.EVENT_BUS.addListener(this::onServerStopped);
		NeoForge.EVENT_BUS.addListener(this::onServerTick);
		NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
		NeoForge.EVENT_BUS.addListener(this::onChat);
		NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event) {
		// Optional: the mod also works when the other side does not have it.
		event.registrar("1").optional().playToClient(SyncPayload.TYPE, SyncPayload.CODEC, (payload, context) -> {
			ClientState.mask = payload.mask();
			ClientState.flags = payload.flags();
		});
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
