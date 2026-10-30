package com.handicap.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import com.handicap.client.InventoryPanel;

import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;

public class HandicapVisualTest implements FabricClientGameTest {
	private static void openInventory(ClientGameTestContext context) {
		LocalPlayer player = context.computeOnClient(mc -> mc.player);
		context.setScreen(() -> new InventoryScreen(player));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("handicap settings timer 10");
			world.getServer().runCommand("handicap give @a broken_arm");
			world.getServer().runCommand("handicap give @a arthritis");
			world.getServer().runCommand("handicap give @a insomnia");
			world.getServer().runCommand("handicap give @a brittle_bones");
			context.waitTicks(60);

			context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(10);
			context.takeScreenshot("cast-third-person-front");

			context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(10);
			context.takeScreenshot("cast-third-person-back");

			context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
			context.waitTicks(10);
			context.takeScreenshot("cast-first-person");

			openInventory(context);
			context.waitTicks(10);
			context.takeScreenshot("inventory-panel");
			context.getInput().setCursorPos(680, 241);
			context.waitTicks(2);
			context.takeScreenshot("inventory-stacking-tooltip");
			context.setScreen(() -> null);

			world.getServer().runCommand("handicap give @a anosognosia");
			context.waitTicks(40);
			context.takeScreenshot("hud-anosognosia");
			openInventory(context);
			context.waitTicks(10);
			context.takeScreenshot("inventory-anosognosia");
			context.setScreen(() -> null);

			world.getServer().runCommand("handicap give @a all");
			world.getServer().runCommand("handicap clear @a anosognosia");
			world.getServer().runCommand("handicap settings stacking false");
			context.waitTicks(40);
			context.takeScreenshot("all-hud");
			openInventory(context);
			context.waitTicks(10);
			context.takeScreenshot("all-inventory-top");
			for (int i = 0; i < 80; i++) {
				context.runOnClient(mc -> {
					int width = mc.getWindow().getGuiScaledWidth();
					int height = mc.getWindow().getGuiScaledHeight();
					int left = (width - 176) / 2;
					InventoryPanel.scroll(left + 176 + 14, height / 2.0, -1.0);
					InventoryPanel.scroll(left - 14, height / 2.0, -1.0);
				});
			}
			context.waitTicks(5);
			context.takeScreenshot("all-inventory-bottom");
			context.setScreen(() -> null);

			world.getServer().runCommand("handicap settings stacking true");
			world.getServer().runCommand("handicap give @a anosognosia");
			context.waitTicks(40);
			context.takeScreenshot("all-with-anosognosia-hud");
			openInventory(context);
			context.waitTicks(10);
			context.takeScreenshot("all-with-anosognosia-inventory");
			context.setScreen(() -> null);
		}
	}
}
