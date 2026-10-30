package com.handicap.fabric.client;

import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;
import com.handicap.SyncPayload;
import com.handicap.client.ClientEffects;
import com.handicap.client.ClientLimbs;
import com.handicap.client.ClientState;
import com.handicap.client.HandicapHud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.KeyMapping;

public class HandicapFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.TYPE, (payload, context) -> {
			ClientState.mask = payload.mask();
			ClientState.flags = payload.flags();
			ClientState.timer = payload.timer();
		});

		ClientPlayNetworking.registerGlobalReceiver(LimbsPayload.TYPE, (payload, context) ->
			ClientLimbs.set(payload.player(), payload.limbs()));

		KeyMapping.Category category = KeyMapping.Category.register(HandicapMod.id("main"));
		KeyMappingHelper.registerKeyMapping(HandicapHud.createKey(category));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HandicapMod.id("list"),
			(graphics, tickCounter) -> HandicapHud.render(graphics));

		ClientTickEvents.START_CLIENT_TICK.register(ClientEffects::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> HandicapHud.tick());

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEffects.onShutdown(client));
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientEffects::onShutdown);
	}
}
