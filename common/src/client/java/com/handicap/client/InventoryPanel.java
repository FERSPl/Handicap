package com.handicap.client;

import com.handicap.Handicap;

import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

public final class InventoryPanel {
	private static final int GAP = 4;
	private static final int PADDING = 6;
	private static final int LINE = 11;
	private static final int BUTTON_HEIGHT = 14;
	private static final int MIN_WIDTH = 96;

	private static final int BACKGROUND = 0xE8141018;
	private static final int BORDER = 0xFF2E2433;
	private static final int ACCENT = 0xFFD23C4B;
	private static final int TITLE = 0xFFFF8A94;
	private static final int MUTED = 0xFF9A90A0;
	private static final int TIME = 0xFFEDEDED;
	private static final int ROW_HOVER = 0x30FFFFFF;
	private static final int BUTTON = 0xFF3A2430;
	private static final int BUTTON_HOVER = 0xFF5A2C3A;

	private static final int[] COLORS = {
		0xFFEDEDED, 0xFF7FD1B9, 0xFFE07A9A, 0xFF8C9EFF, 0xFF6FB7C7,
		0xFFD9B44A, 0xFF7FC97F, 0xFFC792EA, 0xFFF2A65A, 0xFF64B5F6
	};

	private static boolean shown;
	private static int panelX;
	private static int panelY;
	private static int panelW;
	private static int panelH;
	private static int scroll;
	private static int maxScroll;
	private static boolean buttonShown;
	private static int buttonX;
	private static int buttonY;
	private static int buttonW;

	private InventoryPanel() {
	}

	public static boolean supports(Screen screen) {
		return screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
	}

	public static void render(GuiGraphicsExtractor g, int left, int top, int imageWidth, int mouseX, int mouseY) {
		shown = false;
		buttonShown = false;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}
		Font font = mc.font;
		boolean hidden = ClientState.has(Handicap.ANOSOGNOSIA);

		List<Handicap> owned = new ArrayList<>();
		for (Handicap d : Handicap.values()) {
			if (ClientState.has(d)) {
				owned.add(d);
			}
		}

		String title = "Handicaps (" + owned.size() + ")";
		Component timer = timerLine();
		Component stacking = Component.literal("Stacking: ").append(
			Component.literal(ClientState.stacking() ? "On" : "Off").withColor(TIME & 0xFFFFFF));
		boolean canNext = canNext(player);

		int contentW = Math.max(font.width(title), Math.max(font.width(timer), font.width(stacking)));
		for (Handicap d : owned) {
			contentW = Math.max(contentW, font.width(hide(d.displayName, hidden)) + 6);
		}
		if (owned.isEmpty()) {
			contentW = Math.max(contentW, font.width("None"));
		}
		int desired = Math.max(MIN_WIDTH, contentW + PADDING * 2 + 18);
		int right = left + imageWidth + GAP;
		int rightSpace = g.guiWidth() - 2 - right;
		boolean recipeBookOpen = left > (g.guiWidth() - imageWidth) / 2 + 2;
		int leftSpace = recipeBookOpen ? 0 : left - GAP - 2;
		boolean effects = !player.getActiveEffects().isEmpty();
		boolean useLeft;
		if (effects && leftSpace >= Math.min(desired, MIN_WIDTH)) {
			useLeft = true;
		} else if (rightSpace >= Math.min(desired, MIN_WIDTH)) {
			useLeft = false;
		} else {
			useLeft = leftSpace > rightSpace;
		}
		int w = Math.min(desired, useLeft ? leftSpace : rightSpace);
		if (w < 60) {
			return;
		}
		int x = useLeft ? left - GAP - w : right;
		int nameMax = w - PADDING * 2 - 22;

		int header = LINE + 4;
		int footer = 4 + LINE * 2 + (canNext ? BUTTON_HEIGHT + 4 : 0);
		int rows = Math.max(1, owned.size());
		int maxH = g.guiHeight() - 8;
		int h = Math.min(PADDING * 2 + header + rows * LINE + footer, maxH);
		int visible = Math.max(1, (h - PADDING * 2 - header - footer) / LINE);
		maxScroll = Math.max(0, rows - visible);
		scroll = Math.max(0, Math.min(scroll, maxScroll));
		int y = Math.max(4, Math.min(top, g.guiHeight() - 4 - h));

		shown = true;
		panelX = x;
		panelY = y;
		panelW = w;
		panelH = h;

		g.fill(x, y, x + w, y + h, BACKGROUND);
		g.outline(x, y, w, h, BORDER);
		g.fill(x, y, x + 2, y + h, ACCENT);

		int textX = x + PADDING + 2;
		int textY = y + PADDING;
		g.text(font, title, textX, textY, TITLE, false);
		textY += header;

		int listTop = textY;
		if (owned.isEmpty()) {
			g.text(font, "None", textX, textY, MUTED, false);
		} else {
			for (int i = scroll; i < owned.size() && i < scroll + visible; i++) {
				Handicap d = owned.get(i);
				int rowY = listTop + (i - scroll) * LINE;
				boolean hover = mouseX >= x + 2 && mouseX < x + w - 1 && mouseY >= rowY - 1 && mouseY < rowY + LINE - 1;
				if (hover) {
					g.fill(x + 2, rowY - 1, x + w - 1, rowY + LINE - 1, ROW_HOVER);
					tooltip(g, font, d, hidden, mouseX, mouseY);
				}
				g.fill(textX, rowY + 3, textX + 2, rowY + 5, color(d));
				g.text(font, hide(fit(font, d.displayName, nameMax), hidden), textX + 6, rowY, color(d), false);
			}
			if (maxScroll > 0) {
				int trackH = visible * LINE;
				int thumbH = Math.max(6, trackH * visible / rows);
				int thumbY = listTop + (trackH - thumbH) * scroll / maxScroll;
				g.fill(x + w - 4, listTop, x + w - 2, listTop + trackH, 0x40FFFFFF);
				g.fill(x + w - 4, thumbY, x + w - 2, thumbY + thumbH, ACCENT);
			}
		}

		textY = listTop + visible * LINE + 4;
		g.horizontalLine(x + PADDING, x + w - PADDING, textY - 3, BORDER);
		g.text(font, timer, textX, textY, MUTED, false);
		textY += LINE;
		g.text(font, stacking, textX, textY, MUTED, false);
		if (mouseX >= x + 2 && mouseX < x + w - 1 && mouseY >= textY - 1 && mouseY < textY + LINE - 1) {
			g.setTooltipForNextFrame(font, font.split(Component.literal(ClientState.stacking()
				? "Stacking is on: handicaps add up."
				: "Stacking is off: a new handicap replaces the previous one."), 180), mouseX, mouseY);
		}
		textY += LINE;

		if (canNext) {
			buttonShown = true;
			buttonX = x + PADDING;
			buttonY = textY + 2;
			buttonW = w - PADDING * 2;
			boolean hover = mouseX >= buttonX && mouseX < buttonX + buttonW && mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;
			g.fill(buttonX, buttonY, buttonX + buttonW, buttonY + BUTTON_HEIGHT, hover ? BUTTON_HOVER : BUTTON);
			g.outline(buttonX, buttonY, buttonW, BUTTON_HEIGHT, hover ? ACCENT : BORDER);
			g.centeredText(font, "Next handicap", buttonX + buttonW / 2, buttonY + 3, TIME);
		}
	}

	public static boolean click(double mouseX, double mouseY, int button) {
		if (!shown || !inside(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
			return false;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && buttonShown && inside(mouseX, mouseY, buttonX, buttonY, buttonW, BUTTON_HEIGHT)) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
				mc.player.connection.sendCommand("handicap next " + mc.player.getGameProfile().name());
			}
		}
		return true;
	}

	public static boolean scroll(double mouseX, double mouseY, double amount) {
		if (!shown || maxScroll == 0 || !inside(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
			return false;
		}
		scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(amount)));
		return true;
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private static String fit(Font font, String text, int maxWidth) {
		if (font.width(text) <= maxWidth) {
			return text;
		}
		return font.plainSubstrByWidth(text, maxWidth - font.width("…")) + "…";
	}

	static MutableComponent hide(String text, boolean hidden) {
		return hidden ? Component.literal(text).withStyle(ChatFormatting.OBFUSCATED) : Component.literal(text);
	}

	static int color(Handicap d) {
		return COLORS[d.ordinal() % COLORS.length];
	}

	private static void tooltip(GuiGraphicsExtractor g, Font font, Handicap d, boolean hidden, int mouseX, int mouseY) {
		if (!ClientState.showDescriptions()) {
			return;
		}
		List<FormattedCharSequence> lines = new ArrayList<>();
		lines.add(hide(d.displayName, hidden).withColor(color(d) & 0xFFFFFF).getVisualOrderText());
		lines.addAll(font.split(hide(d.description, hidden).withStyle(ChatFormatting.GRAY), 180));
		g.setTooltipForNextFrame(font, lines, mouseX, mouseY);
	}

	static Component timerLine() {
		int ticks = ClientState.timer;
		if (ticks < 0) {
			return Component.literal("Timer off");
		}
		int seconds = (ticks + 19) / 20;
		String time = seconds >= 3600
			? String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
			: String.format("%d:%02d", seconds / 60, seconds % 60);
		return Component.literal("Next in ").append(Component.literal(time).withColor(TIME & 0xFFFFFF));
	}

	private static boolean canNext(LocalPlayer player) {
		var root = player.connection.getCommands().getRoot().getChild("handicap");
		return root != null && root.getChild("next") != null;
	}
}
