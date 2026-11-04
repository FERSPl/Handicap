package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.HandicapConfig;
import com.handicap.HandicapMod;

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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

public final class InventoryPanel {
	private static final int GAP = 4;
	private static final int PADDING = 4;
	private static final int CARD_PAD = 4;
	private static final int RADIUS = 4;
	private static final int LINE = 11;
	private static final int DOT = 7;
	private static final int GEAR = 7;
	private static final String[] GEAR_SHAPE = {
		"..#.#..",
		".#####.",
		"##...##",
		".#...#.",
		"##...##",
		".#####.",
		"..#.#.."
	};
	private static final int BUTTON_HEIGHT = 14;
	private static final int MIN_WIDTH = 96;

	private static final Identifier LOGO = HandicapMod.id("textures/gui/logo.png");
	static final int LOGO_SIZE = 9;
	static final int LOGO_GAP = 3;

	private static final int BACKGROUND = 0xE00C0C0E;
	static final int HUD_BACKGROUND = 0xB80C0C0E;
	static final int CARD = 0x16FFFFFF;
	private static final int ACCENT = 0xFF923F42;
	static final int TITLE = 0xFF923F42;
	static final int TEXT = 0xFFF2F0F3;
	static final int MUTED = 0xFF8C8890;
	private static final int TIME = 0xFF923F42;
	private static final int ROW_HOVER = 0x14FFFFFF;
	private static final int MENU = 0x26923F42;
	private static final int BUTTON = 0x99923F42;
	private static final int BUTTON_HOVER = 0xFFA8494D;

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
	private static int gearX;
	private static int gearY;
	private static boolean menuOpen;
	private static int menuX;
	private static int menuY;
	private static int menuW;

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
			Component.literal(ClientState.stacking()
				? "On (" + (ClientState.maxHandicaps == 0 ? "∞" : ClientState.maxHandicaps) + ")"
				: "Off").withColor(TIME & 0xFFFFFF));
		boolean canNext = canNext(player);

		Component position = positionLine();
		Component background = backgroundLine();
		Component list = listLine();
		int contentW = Math.max(LOGO_SIZE + LOGO_GAP + font.width(title) + 4 + GEAR, Math.max(font.width(timer), font.width(stacking)));
		contentW = Math.max(contentW, Math.max(font.width(position), Math.max(font.width(background), font.width(list))));
		for (Handicap d : owned) {
			contentW = Math.max(contentW, font.width(hide(d.displayName, hidden)) + nameIndent());
		}
		if (owned.isEmpty()) {
			contentW = Math.max(contentW, font.width("None"));
		}
		int desired = Math.max(MIN_WIDTH, contentW + (PADDING + CARD_PAD) * 2 + 10);
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
		int inset = PADDING + CARD_PAD;
		int menuH = CARD_PAD * 2 + LINE * 3 - 2;
		int header = LINE + 2 + (menuOpen ? menuH + 3 : 0);
		int footer = 4 + LINE * 2 + (canNext ? BUTTON_HEIGHT + 4 : 0);
		int chrome = PADDING * 2 + header + CARD_PAD * 2 - 2 + footer;
		int rows = Math.max(1, owned.size());
		int maxH = g.guiHeight() - 8;
		int h = Math.min(chrome + rows * LINE, maxH);
		int visible = Math.max(1, (h - chrome) / LINE);
		maxScroll = Math.max(0, rows - visible);
		scroll = Math.max(0, Math.min(scroll, maxScroll));
		int y = Math.max(4, Math.min(top, g.guiHeight() - 4 - h));
		int nameMax = w - inset * 2 - nameIndent() - (maxScroll > 0 ? 4 : 0);

		shown = true;
		panelX = x;
		panelY = y;
		panelW = w;
		panelH = h;

		roundedFill(g, x, y, w, h, RADIUS, BACKGROUND);

		int textX = x + inset;
		logo(g, textX, y + PADDING + 1);
		g.text(font, title, textX + LOGO_SIZE + LOGO_GAP, y + PADDING + 2, TITLE, false);
		gearX = x + w - inset - GEAR;
		gearY = y + PADDING + 2;
		if (menuOpen) {
			menuX = x + PADDING;
			menuY = y + PADDING + LINE + 2;
			menuW = w - PADDING * 2;
			roundedFill(g, menuX, menuY, menuW, menuH, RADIUS, MENU);
			Component[] options = {position, background, list};
			for (int i = 0; i < options.length; i++) {
				int rowY = menuY + CARD_PAD + i * LINE;
				if (inside(mouseX, mouseY, menuX, rowY - 1, menuW, LINE)) {
					roundedFill(g, menuX + 2, rowY - 2, menuW - 4, LINE + 1, 2, ROW_HOVER);
				}
				g.text(font, options[i], textX, rowY, MUTED, false);
			}
		}
		boolean gearHover = inside(mouseX, mouseY, gearX - 2, gearY - 2, GEAR + 4, GEAR + 4);
		gear(g, gearX, gearY, menuOpen || gearHover ? ACCENT : MUTED);

		int cardX = x + PADDING;
		int cardY = y + PADDING + header;
		int cardW = w - PADDING * 2;
		int cardH = visible * LINE + CARD_PAD * 2 - 2;
		roundedFill(g, cardX, cardY, cardW, cardH, RADIUS, CARD);

		int listTop = cardY + CARD_PAD;
		if (owned.isEmpty()) {
			g.text(font, "None", textX, listTop, MUTED, false);
		} else {
			for (int i = scroll; i < owned.size() && i < scroll + visible; i++) {
				Handicap d = owned.get(i);
				int rowY = listTop + (i - scroll) * LINE;
				boolean hover = mouseX >= cardX && mouseX < cardX + cardW && mouseY >= rowY - 1 && mouseY < rowY + LINE - 1;
				if (hover) {
					roundedFill(g, cardX + 2, rowY - 2, cardW - 4, LINE + 1, 2, ROW_HOVER);
					tooltip(g, font, d, hidden, mouseX, mouseY);
				}
				entry(g, font, hide(fit(font, d.displayName, nameMax), hidden), textX, rowY, color(d));
			}
			if (maxScroll > 0) {
				int trackH = visible * LINE - 2;
				int thumbH = Math.max(6, trackH * visible / rows);
				int thumbY = listTop + (trackH - thumbH) * scroll / maxScroll;
				int barX = cardX + cardW - 5;
				roundedFill(g, barX, listTop, 2, trackH, 1, 0x20FFFFFF);
				roundedFill(g, barX, thumbY, 2, thumbH, 1, ACCENT);
			}
		}

		int textY = cardY + cardH + 4;
		g.text(font, timer, textX, textY, MUTED, false);
		textY += LINE;
		g.text(font, stacking, textX, textY, MUTED, false);
		if (mouseX >= x && mouseX < x + w && mouseY >= textY - 1 && mouseY < textY + LINE - 1) {
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
			roundedFill(g, buttonX, buttonY, buttonW, BUTTON_HEIGHT, RADIUS, hover ? BUTTON_HOVER : BUTTON);
			g.centeredText(font, "Next handicap", buttonX + buttonW / 2, buttonY + 3, TEXT);
		}

	}

	private static Component positionLine() {
		String name = HandicapConfig.hudPosition().replace('_', ' ');
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		return Component.literal("Position: ").append(Component.literal(name).withColor(TIME & 0xFFFFFF));
	}

	private static Component backgroundLine() {
		return Component.literal("Background: ").append(
			Component.literal(HandicapConfig.hudBackground() ? "On" : "Off").withColor(TIME & 0xFFFFFF));
	}

	private static Component listLine() {
		return Component.literal("List: ").append(
			Component.literal(HandicapConfig.coloredNames() ? "Colored text" : "Dots").withColor(TIME & 0xFFFFFF));
	}

	private static void gear(GuiGraphicsExtractor g, int x, int y, int color) {
		for (int row = 0; row < GEAR_SHAPE.length; row++) {
			for (int col = 0; col < GEAR; col++) {
				if (GEAR_SHAPE[row].charAt(col) == '#') {
					g.fill(x + col, y + row, x + col + 1, y + row + 1, color);
				}
			}
		}
	}

	static void roundedFill(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
		int r = Math.min(radius, Math.min(w, h) / 2);
		for (int i = 0; i < r; i++) {
			double dy = r - i - 0.5;
			int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
			g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
			g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
		}
		g.fill(x, y + r, x + w, y + h - r, color);
	}

	static void logo(GuiGraphicsExtractor g, int x, int y) {
		g.blit(RenderPipelines.GUI_TEXTURED, LOGO, x, y, 0, 0, LOGO_SIZE, LOGO_SIZE, 32, 32, 32, 32);
	}

	static int nameIndent() {
		return HandicapConfig.coloredNames() ? 0 : DOT;
	}

	static void entry(GuiGraphicsExtractor g, Font font, Component name, int x, int y, int color) {
		entry(g, font, name, x, y, color, false);
	}

	static void entry(GuiGraphicsExtractor g, Font font, Component name, int x, int y, int color, boolean shadow) {
		if (HandicapConfig.coloredNames()) {
			g.text(font, name, x, y, color, shadow);
		} else {
			dot(g, x, y, color);
			g.text(font, name, x + DOT, y, TEXT, shadow);
		}
	}

	static void dot(GuiGraphicsExtractor g, int x, int y, int color) {
		g.fill(x + 1, y + 2, x + 3, y + 6, color);
		g.fill(x, y + 3, x + 4, y + 5, color);
	}

	public static boolean click(double mouseX, double mouseY, int button) {
		if (!shown || !inside(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
			menuOpen = false;
			return false;
		}
		if (inside(mouseX, mouseY, gearX - 2, gearY - 2, GEAR + 4, GEAR + 4)) {
			menuOpen = !menuOpen;
			clickSound();
			return true;
		}
		if (menuOpen) {
			if (inside(mouseX, mouseY, menuX, menuY + CARD_PAD - 1, menuW, LINE)) {
				HandicapConfig.cycleHudPosition(button == InputConstants.MOUSE_BUTTON_RIGHT ? -1 : 1);
				HandicapConfig.save();
				clickSound();
				return true;
			}
			if (inside(mouseX, mouseY, menuX, menuY + CARD_PAD + LINE - 1, menuW, LINE)) {
				HandicapConfig.setHudBackground(!HandicapConfig.hudBackground());
				HandicapConfig.save();
				clickSound();
				return true;
			}
			if (inside(mouseX, mouseY, menuX, menuY + CARD_PAD + LINE * 2 - 1, menuW, LINE)) {
				HandicapConfig.setColoredNames(!HandicapConfig.coloredNames());
				HandicapConfig.save();
				clickSound();
				return true;
			}
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && buttonShown && inside(mouseX, mouseY, buttonX, buttonY, buttonW, BUTTON_HEIGHT)) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				clickSound();
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

	private static void clickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
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
