package com.handicap.neoforge;

import com.handicap.HandicapMod;
import com.handicap.client.ClientEffects;
import com.handicap.client.HandicapConfigScreen;
import com.handicap.client.HandicapHud;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

// Only loaded on the physical client, so client classes are safe here.
@Mod(value = HandicapMod.MOD_ID, dist = Dist.CLIENT)
public class HandicapNeoForgeClient {
	public HandicapNeoForgeClient(IEventBus modBus, ModContainer container) {
		modBus.addListener(this::registerKeys);
		modBus.addListener(this::registerLayers);

		container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, parent) -> new HandicapConfigScreen(parent));

		NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
		NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
		NeoForge.EVENT_BUS.addListener(this::onLoggingOut);
		NeoForge.EVENT_BUS.addListener(this::onShuttingDown);
	}

	private void registerKeys(RegisterKeyMappingsEvent event) {
		KeyMapping.Category category = new KeyMapping.Category(HandicapMod.id("main"));
		event.registerCategory(category);
		event.register(HandicapHud.createKey(category));
	}

	private void registerLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.CHAT, HandicapMod.id("list"),
			(graphics, deltaTracker) -> HandicapHud.render(graphics));
	}

	private void onClientTickPre(ClientTickEvent.Pre event) {
		ClientEffects.tick(Minecraft.getInstance());
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		HandicapHud.tick();
	}

	private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientEffects.onShutdown(Minecraft.getInstance());
	}

	private void onShuttingDown(GameShuttingDownEvent event) {
		ClientEffects.onShutdown(Minecraft.getInstance());
	}
}
