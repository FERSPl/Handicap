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
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import com.handicap.client.InventoryPanel;

import net.minecraft.client.KeyMapping;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class HandicapFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientLimbs.onShortChange = id -> {
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
			if (mc.level != null) {
				net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(id);
				if (player != null) {
					player.refreshDimensions();
				}
			}
		};
		ClientPlayNetworking.registerGlobalReceiver(SyncPayload.ID, (client, handler, buf, responseSender) -> {
			SyncPayload payload = SyncPayload.read(buf);
			client.execute(() -> ClientState.apply(payload));
		});

		ClientState.sender = payload -> {
			net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
			payload.write(buf);
			ClientPlayNetworking.send(payload.id(), buf);
		};

		ClientPlayNetworking.registerGlobalReceiver(LimbsPayload.ID, (client, handler, buf, responseSender) -> {
			LimbsPayload payload = LimbsPayload.read(buf);
			client.execute(() -> ClientLimbs.set(payload.player(), payload.limbs()));
		});

		EntityModelLayerRegistry.registerModelLayer(WheelchairModel.LAYER, WheelchairModel::createBodyLayer);
		EntityRendererRegistry.register(HandicapContent.wheelchair(), WheelchairRenderer::new);

		KeyBindingHelper.registerKeyBinding(HandicapHud.createKey("key.category.handicap.main"));

		HudRenderCallback.EVENT.register((graphics, tickCounter) -> HandicapHud.render(graphics));

		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (InventoryPanel.supports(screen)) {
				ScreenMouseEvents.allowMouseScroll(screen).register((s, mouseX, mouseY, horizontal, vertical) ->
					!InventoryPanel.scroll(mouseX, mouseY, vertical));
			}
		});

		ClientTickEvents.START_CLIENT_TICK.register(ClientEffects::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> HandicapHud.tick());

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientEffects.onShutdown(client));
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientEffects::onShutdown);
	}
}
