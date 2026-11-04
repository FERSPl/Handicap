package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.HandicapConfig;

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
	private static final int PADDING = 4;
	private static final int CARD_PAD = 4;
	private static final int RADIUS = 4;
	private static final int MAX_LINES = 12;
	private static final float SCALE = 0.75f;
	private static final int LINE_HEIGHT = 10;
	private static final int EFFECTS_HEIGHT = 54;

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
		if (!visible || mc.player == null || InventoryPanel.supports(mc.screen)) {
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
		int maxLines = Math.max(1, Math.min(MAX_LINES, (screenHeight - 24 - PADDING * 2 - LINE_HEIGHT - 2 - CARD_PAD * 2 - footer) / LINE_HEIGHT));
		int more = 0;
		if (owned.size() > maxLines) {
			more = owned.size() - (maxLines - 1);
			owned = new ArrayList<>(owned.subList(0, maxLines - 1));
		}

		String title = "Handicaps (" + (owned.size() + more) + ")";
		Component timer = InventoryPanel.timerLine();
		int width = InventoryPanel.LOGO_SIZE + InventoryPanel.LOGO_GAP + font.width(title);
		for (Handicap d : owned) {
			width = Math.max(width, font.width(d.displayName) + InventoryPanel.nameIndent());
		}
		if (more > 0) {
			width = Math.max(width, font.width("+" + more + " more"));
		}
		if (timerOn) {
			width = Math.max(width, font.width(timer));
		}
		int lines = owned.size() + (more > 0 ? 1 : 0);
		int inset = PADDING + CARD_PAD;
		int cardH = CARD_PAD * 2 + lines * LINE_HEIGHT - 1;
		int boxWidth = width + inset * 2;
		int boxHeight = PADDING * 2 + LINE_HEIGHT + 2 + cardH + footer;
		String position = HandicapConfig.hudPosition();
		boolean right = position.endsWith("right");
		int x = right ? screenWidth - boxWidth - 4 : 4;
		int y;
		if (position.startsWith("top")) {
			// Stay clear of the vanilla effect icons in the top right corner.
			y = right && !mc.player.getActiveEffects().isEmpty() ? (int) (EFFECTS_HEIGHT / SCALE) : 4;
		} else if (position.startsWith("bottom")) {
			y = screenHeight - boxHeight - 4;
		} else {
			y = (screenHeight - boxHeight) / 2;
		}
		y = Math.max(4, y);
		boolean background = HandicapConfig.hudBackground();

		graphics.pose().pushMatrix();
		graphics.pose().scale(SCALE, SCALE);
		if (background) {
			InventoryPanel.roundedFill(graphics, x, y, boxWidth, boxHeight, RADIUS, InventoryPanel.HUD_BACKGROUND);
		}
		int textX = x + inset;
		InventoryPanel.logo(graphics, textX, y + PADDING);
		graphics.text(font, title, textX + InventoryPanel.LOGO_SIZE + InventoryPanel.LOGO_GAP, y + PADDING + 1, InventoryPanel.TITLE, !background);
		int cardY = y + PADDING + LINE_HEIGHT + 2;
		if (background) {
			InventoryPanel.roundedFill(graphics, x + PADDING, cardY, boxWidth - PADDING * 2, cardH, RADIUS, InventoryPanel.CARD);
		}
		int textY = cardY + CARD_PAD;
		for (Handicap d : owned) {
			InventoryPanel.entry(graphics, font, Component.literal(d.displayName), textX, textY, InventoryPanel.color(d), !background);
			textY += LINE_HEIGHT;
		}
		if (more > 0) {
			graphics.text(font, "+" + more + " more", textX, textY, InventoryPanel.MUTED, !background);
		}
		if (timerOn) {
			graphics.text(font, timer, textX, cardY + cardH + 4, InventoryPanel.MUTED, !background);
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
