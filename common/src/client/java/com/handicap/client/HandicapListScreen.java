package com.handicap.client;

import com.handicap.Handicap;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandicapListScreen extends Screen {
	private static final int ROW = 22;
	private static final int TOP = 32;

	private final Screen parent;
	private final List<Button> rows = new ArrayList<>();
	private int scroll;
	private int visible;

	public HandicapListScreen(Screen parent) {
		super(Component.literal("Disabled Handicaps"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		Handicap[] all = Handicap.values();
		visible = Math.max(1, (this.height - TOP - 36) / ROW);
		scroll = Math.max(0, Math.min(scroll, all.length - visible));
		boolean edit = this.minecraft.player != null && InventoryPanel.canNext(this.minecraft.player);
		rows.clear();
		for (int i = 0; i < visible && scroll + i < all.length; i++) {
			Handicap d = all[scroll + i];
			Button.Builder builder = Button.builder(Component.empty(), b -> HandicapSettingsScreen.send("disable:" + d.id, ClientState.disabled(d) ? 0 : 1))
				.bounds(this.width / 2 - 120, TOP + i * ROW, 240, 20)
				.tooltip(Tooltip.create(Component.literal(edit ? d.description : "You don't have the permission to change this")));
			Button button = addRenderableWidget(builder.build());
			button.active = edit;
			rows.add(button);
		}
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
		refresh();
	}

	private void refresh() {
		Handicap[] all = Handicap.values();
		for (int i = 0; i < rows.size(); i++) {
			Handicap d = all[scroll + i];
			boolean off = ClientState.disabled(d);
			rows.get(i).setMessage(Component.literal(d.displayName + ": ")
				.append(Component.literal(off ? "Disabled" : "Enabled").withStyle(off ? ChatFormatting.RED : ChatFormatting.GREEN)));
		}
	}

	@Override
	public void tick() {
		refresh();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		scroll -= (int) Math.signum(scrollY) * 3;
		rebuildWidgets();
		return true;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		int total = Handicap.values().length;
		if (total > visible) {
			int x = this.width / 2 + 126;
			int trackH = visible * ROW - 2;
			int thumbH = Math.max(10, trackH * visible / total);
			int thumbY = TOP + (trackH - thumbH) * scroll / (total - visible);
			graphics.fill(x, TOP, x + 4, TOP + trackH, 0x60000000);
			graphics.fill(x, thumbY, x + 4, thumbY + thumbH, InventoryPanel.TITLE);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}
}
