package com.handicap.fabric.client;

import com.handicap.HandicapContent;
import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;
import com.handicap.SyncPayload;
import com.handicap.client.ClientEffects;
import com.handicap.client.ClientLimbs;
import com.handicap.client.ClientState;
import com.handicap.client.HandicapHud;
import com.handicap.client.WheelchairModel;
import com.handicap.client.WheelchairRenderer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class HandicapFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.TYPE, (payload, context) -> {
			ClientState.apply(payload);
		});

		ClientState.sender = ClientPlayNetworking::send;

		ClientPlayNetworking.registerGlobalReceiver(LimbsPayload.TYPE, (payload, context) ->
			ClientLimbs.set(payload.player(), payload.limbs()));

		EntityModelLayerRegistry.registerModelLayer(WheelchairModel.LAYER, WheelchairModel::createBodyLayer);
		EntityRenderers.register(HandicapContent.wheelchair(), WheelchairRenderer::new);

		KeyBindingHelper.registerKeyBinding(HandicapHud.createKey("key.category.handicap.main"));

		HudRenderCallback.EVENT.register((graphics, tickCounter) -> HandicapHud.render(graphics));

		ClientTickEvents.START_CLIENT_TICK.register(ClientEffects::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> HandicapHud.tick());

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEffects.onShutdown(client));
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientEffects::onShutdown);
	}
}
