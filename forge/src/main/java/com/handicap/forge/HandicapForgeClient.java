package com.handicap.forge;

import com.handicap.HandicapContent;
import com.handicap.HandicapMod;
import com.handicap.client.ClientEffects;
import com.handicap.client.ClientLimbs;
import com.handicap.client.ClientState;
import com.handicap.client.HandicapHud;
import com.handicap.client.HandicapSettingsScreen;
import com.handicap.client.WheelchairModel;
import com.handicap.client.WheelchairRenderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import com.handicap.client.InventoryPanel;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

public final class HandicapForgeClient {
	private HandicapForgeClient() {
	}

	public static void init(IEventBus modBus) {
		ClientLimbs.onShortChange = id -> {
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
			if (mc.level != null) {
				net.minecraft.world.entity.player.Player player = mc.level.getPlayerByUUID(id);
				if (player != null) {
					player.refreshDimensions();
				}
			}
		};
		ClientState.sender = payload -> HandicapForge.CHANNEL.sendToServer(payload);
		modBus.addListener(HandicapForgeClient::registerKeys);
		modBus.addListener(HandicapForgeClient::registerOverlays);
		modBus.addListener(HandicapForgeClient::registerModelLayers);
		modBus.addListener(HandicapForgeClient::registerRenderers);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new HandicapSettingsScreen(parent)));
		MinecraftForge.EVENT_BUS.addListener(HandicapForgeClient::onClientTick);
		MinecraftForge.EVENT_BUS.addListener(HandicapForgeClient::onScroll);
		MinecraftForge.EVENT_BUS.addListener(HandicapForgeClient::onLoggingOut);
		MinecraftForge.EVENT_BUS.addListener(HandicapForgeClient::onShuttingDown);
	}

	private static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(HandicapHud.createKey("key.category.handicap.main"));
	}

	private static void registerOverlays(RegisterGuiOverlaysEvent event) {
		event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(), "list",
			(gui, graphics, partialTick, width, height) -> HandicapHud.render(graphics));
	}

	private static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(WheelchairModel.LAYER, WheelchairModel::createBodyLayer);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(HandicapContent.wheelchair(), WheelchairRenderer::new);
	}

	private static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			ClientEffects.tick(Minecraft.getInstance());
		} else {
			HandicapHud.tick();
		}
	}

	private static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
		if (InventoryPanel.supports(event.getScreen())
				&& InventoryPanel.scroll(event.getMouseX(), event.getMouseY(), event.getScrollDelta())) {
			event.setCanceled(true);
		}
	}

	private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientEffects.onShutdown(Minecraft.getInstance());
	}

	private static void onShuttingDown(GameShuttingDownEvent event) {
		ClientEffects.onShutdown(Minecraft.getInstance());
	}
}
