package com.handicap.client;

import com.handicap.Handicap;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public final class HandicapHud {
	private static final int PADDING = 5;
	private static final int MAX_LINES = 12;
	private static final float SCALE = 0.75f;
	private static final int LINE_HEIGHT = 10;
	private static final int BACKGROUND = 0xC0141018;
	private static final int BORDER = 0xFF2E2433;
	private static final int ACCENT = 0xFFD23C4B;
	private static final int TITLE = 0xFFFF8A94;
	private static final int MUTED = 0xFF9A90A0;

	private static boolean visible = true;
	private static KeyMapping toggleKey;

	private HandicapHud() {
	}

	public static KeyMapping createKey(KeyMapping.Category category) {
		toggleKey = new KeyMapping(
			"key.handicap.toggle_list",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_GRAVE_ACCENT,
			category);
		return toggleKey;
	}

	public static void render(GuiGraphicsExtractor graphics) {
		Minecraft mc = Minecraft.getInstance();
		if (!visible || mc.player == null || InventoryPanel.supports(mc.gui.screen())) {
			return;
		}
		if (ClientState.has(Handicap.ANOSOGNOSIA)) {
			return;
		}
		List<Handicap> owned = new ArrayList<>();
		for (Handicap d : Handicap.values()) {
			if (ClientState.has(d)) {
				owned.add(d);
			}
		}
		boolean timerOn = ClientState.timer >= 0;
		if (owned.isEmpty() && !timerOn) {
			return;
		}
		Font font = mc.font;
		int screenWidth = (int) (graphics.guiWidth() / SCALE);
		int screenHeight = (int) (graphics.guiHeight() / SCALE);
		int footer = timerOn ? LINE_HEIGHT + 4 : 0;
		int maxLines = Math.max(1, Math.min(MAX_LINES, (screenHeight - 24 - PADDING * 2 - LINE_HEIGHT - 3 - footer) / LINE_HEIGHT));
		int more = 0;
		if (owned.size() > maxLines) {
			more = owned.size() - (maxLines - 1);
			owned = new ArrayList<>(owned.subList(0, maxLines - 1));
		}

		String title = "Handicaps (" + (owned.size() + more) + ")";
		Component timer = InventoryPanel.timerLine();
		int width = font.width(title);
		for (Handicap d : owned) {
			width = Math.max(width, font.width(d.displayName) + 6);
		}
		if (more > 0) {
			width = Math.max(width, font.width("+" + more + " more"));
		}
		if (timerOn) {
			width = Math.max(width, font.width(timer));
		}
		int lines = owned.size() + (more > 0 ? 1 : 0);
		int boxWidth = width + PADDING * 2 + 2;
		int boxHeight = PADDING * 2 + LINE_HEIGHT + 3 + lines * LINE_HEIGHT + footer;
		int x = screenWidth - boxWidth - 4;
		int y = Math.max(4, (screenHeight - boxHeight) / 2);

		graphics.pose().pushMatrix();
		graphics.pose().scale(SCALE, SCALE);
		graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND);
		graphics.outline(x, y, boxWidth, boxHeight, BORDER);
		graphics.fill(x, y, x + 2, y + boxHeight, ACCENT);
		int textX = x + PADDING + 2;
		int textY = y + PADDING;
		graphics.text(mc.font, title, textX, textY, TITLE, false);
		textY += LINE_HEIGHT + 3;
		for (Handicap d : owned) {
			int color = InventoryPanel.color(d);
			graphics.fill(textX, textY + 3, textX + 2, textY + 5, color);
			graphics.text(font, d.displayName, textX + 6, textY, color, false);
			textY += LINE_HEIGHT;
		}
		if (more > 0) {
			graphics.text(font, "+" + more + " more", textX, textY, MUTED, false);
			textY += LINE_HEIGHT;
		}
		if (timerOn) {
			graphics.horizontalLine(x + PADDING, x + boxWidth - PADDING, textY + 1, BORDER);
			graphics.text(font, timer, textX, textY + 4, MUTED, false);
		}
		graphics.pose().popMatrix();
	}

	public static void tick() {
		if (ClientState.timer > 0 && !Minecraft.getInstance().isPaused()) {
			ClientState.timer--;
		}
		if (toggleKey == null) {
			return;
		}
		while (toggleKey.consumeClick()) {
			visible = !visible;
		}
	}
}
