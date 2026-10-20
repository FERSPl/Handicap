package com.handicap.client;

import com.handicap.SyncPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class HandicapModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.TYPE, (payload, context) -> {
			ClientState.mask = payload.mask();
			ClientState.flags = payload.flags();
		});

		HandicapHud.register();

		ClientTickEvents.START_CLIENT_TICK.register(ClientEffects::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> HandicapHud.tick());

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEffects.onShutdown(client));
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientEffects::onShutdown);
	}
}
