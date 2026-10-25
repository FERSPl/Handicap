package com.handicap.fabric.client;

import com.handicap.HandicapConfig;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandicapConfigScreen extends Screen {
	private final Screen parent;

	public HandicapConfigScreen(Screen parent) {
		super(Component.literal("Handicap"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		addRenderableWidget(Button.builder(levelLabel(), button -> {
			HandicapConfig.setPermissionLevel(HandicapConfig.permissionLevel() == 0 ? 2 : 0);
			button.setMessage(levelLabel());
		}).bounds(centerX - 100, this.height / 2 - 24, 200, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
			.bounds(centerX - 100, this.height / 2 + 4, 200, 20).build());

		String[] notes = {
			"Player: everyone can use /handicap. Op: operators only.",
			"With cheats off and permission = Player, you can only target",
			"yourself (by name, no @a / @s), and still manage the timer."
		};
		for (int i = 0; i < notes.length; i++) {
			addRenderableWidget(new StringWidget(centerX - 160, this.height / 2 + 36 + i * 12, 320, 10,
				Component.literal(notes[i]), this.font));
		}
	}

	@Override
	public void onClose() {
		HandicapConfig.save();
		this.minecraft.gui.setScreen(parent);
	}

	private static Component levelLabel() {
		int level = HandicapConfig.permissionLevel();
		String name = level == 0 ? "Player" : level == 2 ? "Op" : "Level " + level;
		return Component.literal("Command permission: " + name);
	}
}
