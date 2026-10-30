package com.handicap.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

public class DyslexiaMenuTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("handicap give @a dyslexia");
			context.waitTicks(30);
			context.setScreen(() -> new PauseScreen(true));
			context.waitTicks(5);
			context.takeScreenshot("dyslexia-pause-with");
			context.setScreen(() -> null);
			world.getServer().runCommand("handicap clear @a dyslexia");
			context.waitTicks(30);
			context.setScreen(() -> new PauseScreen(true));
			context.waitTicks(5);
			context.takeScreenshot("dyslexia-pause-after");
			Options options = context.computeOnClient(mc -> mc.options);
			context.setScreen(() -> new OptionsScreen(null, options));
			context.waitTicks(5);
			context.takeScreenshot("dyslexia-options-after");
			context.setScreen(() -> null);
		}
		context.waitTicks(5);
		context.takeScreenshot("dyslexia-title-after");
	}
}
