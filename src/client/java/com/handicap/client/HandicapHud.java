package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.HandicapMod;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import org.lwjgl.glfw.GLFW;

public final class HandicapHud {
	private static final int PADDING = 3;
	private static final int LINE_HEIGHT = 9;
	private static final int BACKGROUND = 0xB0141018;
	private static final int ACCENT = 0xFFD23C4B;
	private static final int TITLE = 0xFFFF8A94;
	private static final int TEXT = 0xFFEDEDED;

	private static boolean visible = true;
	private static KeyMapping toggleKey;

	private HandicapHud() {
	}

	public static void register() {
		KeyMapping.Category category = KeyMapping.Category.register(HandicapMod.id("main"));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.handicap.toggle_list",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_GRAVE_ACCENT,
			category));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, HandicapMod.id("list"),
			(graphics, tickCounter) -> {
				if (!visible || ClientState.has(Handicap.ANOSOGNOSIA)) {
					return;
				}
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null) {
					return;
				}
				List<String> names = new ArrayList<>();
				for (Handicap d : Handicap.values()) {
					if (ClientState.has(d)) {
						names.add(d.displayName);
					}
				}
				if (names.isEmpty()) {
					return;
				}
				String title = "Handicaps (" + names.size() + ")";
				int width = mc.font.width(title);
				for (String name : names) {
					width = Math.max(width, mc.font.width(name));
				}
				int screenWidth = mc.getWindow().getGuiScaledWidth();
				int screenHeight = mc.getWindow().getGuiScaledHeight();
				int maxLines = Math.max(1, (screenHeight - 24 - PADDING * 2 - LINE_HEIGHT - 2) / LINE_HEIGHT);
				if (names.size() > maxLines) {
					int hidden = names.size() - (maxLines - 1);
					names = new ArrayList<>(names.subList(0, maxLines - 1));
					names.add("+" + hidden + " more");
					for (String name : names) {
						width = Math.max(width, mc.font.width(name));
					}
				}
				int boxWidth = width + PADDING * 2 + 3;
				int boxHeight = PADDING * 2 + LINE_HEIGHT + 2 + names.size() * LINE_HEIGHT;
				int x = screenWidth - boxWidth - 2;
				int y = Math.max(2, (screenHeight - boxHeight) / 2);
				graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND);
				graphics.fill(x, y, x + 2, y + boxHeight, ACCENT);
				int textX = x + PADDING + 3;
				int textY = y + PADDING;
				graphics.text(mc.font, title, textX, textY, TITLE, false);
				textY += LINE_HEIGHT + 2;
				for (String name : names) {
					graphics.text(mc.font, name, textX, textY, TEXT, false);
					textY += LINE_HEIGHT;
				}
			});
	}

	public static void tick() {
		if (toggleKey == null) {
			return;
		}
		while (toggleKey.consumeClick()) {
			visible = !visible;
		}
	}
}
