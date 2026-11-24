package com.handicap.forge;

import com.handicap.HandicapCommands;
import com.handicap.HandicapContent;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.HandicapPayload;
import com.handicap.HandicapRules;
import com.handicap.LimbsPayload;
import com.handicap.SettingsPayload;
import com.handicap.SyncPayload;
import com.handicap.WheelchairEntity;
import com.handicap.client.ClientLimbs;
import com.handicap.client.ClientState;

import java.nio.file.Path;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.RegisterEvent;

@Mod(HandicapMod.MOD_ID)
public class HandicapForge {
	private static final String PROTOCOL = "3";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(HandicapMod.id("main"), () -> PROTOCOL,
		NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));

	public HandicapForge() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		registerMessages();
		HandicapMod.init(new HandicapMod.Platform() {
			@Override
			public boolean canSend(ServerPlayer player) {
				return player.connection != null && player.connection.connection.channel() != null
					&& CHANNEL.isRemotePresent(player.connection.connection);
			}

			@Override
			public void send(ServerPlayer player, HandicapPayload payload) {
				CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
			}

			@Override
			public Path configDir() {
				return FMLPaths.CONFIGDIR.get();
			}
		});

		modBus.addListener(this::registerContent);
		modBus.addListener(this::registerAttributes);
		modBus.addListener(this::addToCreativeTab);

		MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStopped);
		MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
		MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
		MinecraftForge.EVENT_BUS.addListener(this::onChat);
		MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
		MinecraftForge.EVENT_BUS.addListener(this::onBreakSpeed);

		if (FMLEnvironment.dist == Dist.CLIENT) {
			HandicapForgeClient.init(modBus);
		}
	}

	private static void registerMessages() {
		CHANNEL.messageBuilder(SyncPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(SyncPayload::write)
			.decoder(SyncPayload::read)
			.consumerMainThread((payload, context) -> ClientState.apply(payload))
			.add();
		CHANNEL.messageBuilder(LimbsPayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(LimbsPayload::write)
			.decoder(LimbsPayload::read)
			.consumerMainThread((payload, context) -> ClientLimbs.set(payload.player(), payload.limbs()))
			.add();
		CHANNEL.messageBuilder(SettingsPayload.class, 2, NetworkDirection.PLAY_TO_SERVER)
			.encoder(SettingsPayload::write)
			.decoder(SettingsPayload::read)
			.consumerMainThread((payload, context) -> {
				ServerPlayer player = context.get().getSender();
				if (player != null) {
					HandicapManager.applySetting(player, payload.key(), payload.value());
				}
			})
			.add();
	}

	private void registerContent(RegisterEvent event) {
		event.register(Registries.ENTITY_TYPE, helper ->
			helper.register(HandicapContent.WHEELCHAIR_ID, HandicapContent.createEntity()));
		event.register(Registries.SOUND_EVENT, helper ->
			helper.register(HandicapContent.FLATULENCE_SOUND_ID, HandicapContent.createFlatulenceSound()));
		event.register(Registries.SOUND_EVENT, helper ->
			helper.register(HandicapContent.NEW_HANDICAP_SOUND_ID, HandicapContent.createNewHandicapSound()));
		event.register(Registries.ITEM, helper ->
			helper.register(HandicapContent.WHEELCHAIR_ID, HandicapContent.createItem()));
	}

	private void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(HandicapContent.wheelchair(), WheelchairEntity.createAttributes().build());
	}

	private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == HandicapContent.TOOLS_TAB) {
			event.accept(HandicapContent.wheelchairItem());
		}
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

	private void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			HandicapManager.onServerTick(event.getServer());
		}
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

	private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		float factor = HandicapRules.breakFactor(event.getEntity());
		if (factor != 1.0f) {
			event.setNewSpeed(event.getNewSpeed() * factor);
		}
	}
}
