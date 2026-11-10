package com.handicap.client;

import com.handicap.HandicapConfig;
import com.handicap.SettingsPayload;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandicapSettingsScreen extends Screen {
	private static final int[] TIMER = {0, 60, 120, 300, 600, 900, 1200, 1800, 3600};
	private static final int[] MAX = {0, 1, 2, 3, 4, 5, 6, 8, 10, 15, 20};
	private static final int[] DURATION = {0, 60, 120, 300, 600, 900, 1800, 3600};
	private static final int HEADER = InventoryPanel.TITLE;

	private final Screen parent;
	private Button position;
	private Button background;
	private Button list;
	private Button sync;
	private Button stacking;
	private Button timer;
	private Button max;
	private Button duration;
	private Button descriptions;
	private int left;
	private int displayY;
	private int rulesY;

	public HandicapSettingsScreen(Screen parent) {
		super(Component.literal("Handicap Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		left = this.width / 2 - 155;
		int y = Math.max(30, this.height / 2 - 105);
		displayY = y;
		y += 12;
		position = addRenderableWidget(Button.builder(Component.empty(), b -> {
			HandicapConfig.cycleHudPosition(1);
			HandicapConfig.save();
		}).bounds(left, y, 150, 20).tooltip(tip("Where the handicap list is shown on your screen.")).build());
		background = addRenderableWidget(Button.builder(Component.empty(), b -> {
			HandicapConfig.setHudBackground(!HandicapConfig.hudBackground());
			HandicapConfig.save();
		}).bounds(left + 160, y, 150, 20).tooltip(tip("Show a dark background behind the on-screen handicap list.")).build());
		y += 24;
		list = addRenderableWidget(Button.builder(Component.empty(), b -> {
			HandicapConfig.setColoredNames(!HandicapConfig.coloredNames());
			HandicapConfig.save();
		}).bounds(left, y, 150, 20).tooltip(tip("Show handicap names in color, or with a colored dot.")).build());
		y += 32;
		rulesY = y;
		y += 12;
		boolean edit = this.minecraft.player != null && InventoryPanel.canNext(this.minecraft.player);
		sync = rule(edit, left, y, "Everyone gets the same handicaps: when a player gets or loses one, all players do.",
			b -> send("sync", ClientState.sync() ? 0 : 1));
		stacking = rule(edit, left + 160, y, "On: handicaps add up. Off: a new handicap replaces the previous one.",
			b -> send("stacking", ClientState.stacking() ? 0 : 1));
		y += 24;
		timer = rule(edit, left, y, "How often everyone gets a new random handicap.",
			b -> send("timer", next(TIMER, ClientState.timer < 0 ? 0 : ClientState.interval)));
		max = rule(edit, left + 160, y, "The most handicaps a player can have. When a new one goes over it, the oldest is removed.",
			b -> send("max", next(MAX, ClientState.maxHandicaps)));
		y += 24;
		duration = rule(edit, left, y, "How long each handicap lasts before it wears off on its own.",
			b -> send("duration", next(DURATION, ClientState.duration)));
		descriptions = rule(edit, left + 160, y, "Show what a handicap does when hovering it in the inventory. Applies to every world.",
			b -> send("descriptions", ClientState.showDescriptions() ? 0 : 1));
		y += 24;
		addRenderableWidget(Button.builder(Component.literal("Disabled handicaps..."), b -> this.minecraft.gui.setScreen(new HandicapListScreen(this)))
			.bounds(left, y, 310, 20).tooltip(tip("Choose which handicaps can never be given in this world.")).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(this.width / 2 - 100, Math.min(this.height - 28, y + 36), 200, 20).build());
		refresh();
	}

	private Button rule(boolean edit, int x, int y, String help, Button.OnPress press) {
		Button.Builder builder = Button.builder(Component.empty(), press).bounds(x, y, 150, 20)
			.tooltip(tip(edit ? help : help + " You don't have the permission to change this."));
		Button button = addRenderableWidget(builder.build());
		button.active = edit;
		return button;
	}

	private void refresh() {
		String pos = HandicapConfig.hudPosition().replace('_', ' ');
		position.setMessage(Component.literal("Position: " + Character.toUpperCase(pos.charAt(0)) + pos.substring(1)));
		background.setMessage(Component.literal("Background: " + (HandicapConfig.hudBackground() ? "On" : "Off")));
		list.setMessage(Component.literal("List: " + (HandicapConfig.coloredNames() ? "Colored text" : "Dots")));
		sync.setMessage(Component.literal("Synchronization: " + (ClientState.sync() ? "On" : "Off")));
		stacking.setMessage(Component.literal("Stacking: " + (ClientState.stacking() ? "On" : "Off")));
		timer.setMessage(Component.literal("Timer: " + time(ClientState.timer < 0 ? 0 : ClientState.interval)));
		max.setMessage(Component.literal("Max: " + (ClientState.maxHandicaps <= 0 ? "Off" : String.valueOf(ClientState.maxHandicaps))));
		descriptions.setMessage(Component.literal("Descriptions: " + (ClientState.showDescriptions() ? "On" : "Off")));
		duration.setMessage(Component.literal("Duration: " + time(ClientState.duration)));
	}

	@Override
	public void tick() {
		refresh();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		graphics.text(this.font, "Display", left, displayY, HEADER, true);
		graphics.text(this.font, "Game rules", left, rulesY, HEADER, true);
	}

	@Override
	public void onClose() {
		HandicapConfig.save();
		this.minecraft.gui.setScreen(parent);
	}

	private static Tooltip tip(String text) {
		return Tooltip.create(Component.literal(text));
	}

	public static void send(String key, int value) {
		if (ClientState.sender != null) {
			ClientState.sender.accept(new SettingsPayload(key, value));
		}
	}

	private static int next(int[] steps, int current) {
		for (int step : steps) {
			if (step > current) {
				return step;
			}
		}
		return steps[0];
	}

	private static String time(int seconds) {
		if (seconds <= 0) {
			return "Off";
		}
		return seconds % 60 == 0 ? seconds / 60 + " min" : seconds + " s";
	}
}
