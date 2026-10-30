package com.handicap.fabric.gametest;

import com.handicap.HandicapManager;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class SettingsTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("handicap settings max 3");
			for (int i = 0; i < 6; i++) {
				world.getServer().runCommand("handicap next @a");
				context.waitTicks(25);
			}
			check(world, 3, "max 3 after 6 next");

			world.getServer().runCommand("handicap give @a all");
			context.waitTicks(5);
			check(world, 63, "give all ignores max");
			world.getServer().runCommand("handicap next @a");
			context.waitTicks(5);
			check(world, 3, "next brings everything back to max");

			world.getServer().runCommand("handicap settings stacking false");
			world.getServer().runCommand("handicap give @a all");
			world.getServer().runCommand("handicap next @a");
			context.waitTicks(5);
			check(world, 1, "next with stacking off and every handicap");
			world.getServer().runCommand("handicap settings stacking true");

			world.getServer().runCommand("handicap clear @a");
			world.getServer().runCommand("handicap settings max off");
			world.getServer().runCommand("handicap settings duration 0.1");
			world.getServer().runCommand("handicap next @a");
			world.getServer().runCommand("handicap next @a");
			context.waitTicks(20);
			check(world, 2, "two handicaps before they wear off");
			context.waitTicks(160);
			check(world, 0, "duration 6 s wore them off");

			world.getServer().runCommand("handicap settings duration off");
			float normal = strafe(context);
			String leftKey = context.computeOnClient(mc -> mc.options.keyLeft.saveString());
			world.getServer().runCommand("handicap give @a spatial_disorientation");
			context.waitTicks(30);
			float swapped = strafe(context);
			if (normal == 0 || swapped != -normal) {
				throw new AssertionError("spatial disorientation: left gave " + normal + " normally and " + swapped + " with the handicap");
			}
			if (!leftKey.equals(context.computeOnClient(mc -> mc.options.keyLeft.saveString()))) {
				throw new AssertionError("spatial disorientation changed the key bindings");
			}
			world.getServer().runCommand("handicap clear @a");
			context.waitTicks(30);
			world.getServer().runCommand("handicap settings");
			context.waitTicks(5);
			context.takeScreenshot("settings-status");
		}
	}

	private static float strafe(ClientGameTestContext context) {
		context.getInput().holdKey(options -> options.keyLeft);
		context.waitTicks(2);
		float x = context.computeOnClient(mc -> mc.player.input.getMoveVector().x);
		context.getInput().releaseKey(options -> options.keyLeft);
		context.waitTicks(2);
		return x;
	}

	private static void check(TestSingleplayerContext world, int expected, String label) {
		int count = world.getServer().computeOnServer(server ->
			HandicapManager.state().get(server.getPlayerList().getPlayers().get(0).getUUID()).size());
		if (count != expected) {
			throw new AssertionError(label + ": expected " + expected + " handicaps, got " + count);
		}
	}
}
