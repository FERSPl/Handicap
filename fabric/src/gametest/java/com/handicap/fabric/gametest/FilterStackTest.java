package com.handicap.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class FilterStackTest implements FabricClientGameTest {
	private static final String[] FILTERS = {"color_blind", "tunnel_vision", "derealization", "inverted_vision"};

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("time set day");
			world.getServer().runCommand("handicap settings timer off");
			context.waitTicks(20);
			context.takeScreenshot("filters-0");
			for (int i = 0; i < FILTERS.length; i++) {
				world.getServer().runCommand("handicap give @a " + FILTERS[i]);
				context.waitTicks(40);
				context.takeScreenshot("filters-" + (i + 1) + "-" + FILTERS[i]);
				int expected = i + 1;
				int applied = context.computeOnClient(mc -> mc.gameRenderer.getAppliedPostEffects().size());
				if (applied != expected) {
					throw new AssertionError("expected " + expected + " filters applied, got " + applied);
				}
			}
		}
	}
}
