package com.handicap.fabric.gametest;

import com.handicap.HandicapContent;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;

public class NewHandicapsTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("handicap give @a stiff_neck");
			context.waitTicks(30);
			float up = pitchAfterTurn(context, -600);
			float down = pitchAfterTurn(context, 600);
			if (up < -5.0f || down > 5.0f) {
				throw new AssertionError("stiff neck: pitch went from " + up + " to " + down);
			}
			world.getServer().runCommand("handicap clear @a");
			context.waitTicks(30);
			float free = pitchAfterTurn(context, 600);
			if (free <= 5.0f) {
				throw new AssertionError("without stiff neck the pitch stayed at " + free);
			}
			pitchAfterTurn(context, -free / 0.15);

			boolean registered = context.computeOnClient(mc ->
				BuiltInRegistries.SOUND_EVENT.containsKey(HandicapContent.FLATULENCE_SOUND_ID));
			boolean loaded = context.computeOnClient(mc ->
				mc.getSoundManager().getSoundEvent(HandicapContent.FLATULENCE_SOUND_ID) != null);
			if (!registered || !loaded) {
				throw new AssertionError("flatulence sound: registered=" + registered + " loaded=" + loaded);
			}
			int variants = context.computeOnClient(mc ->
				mc.getSoundManager().getSoundEvent(HandicapContent.FLATULENCE_SOUND_ID).getWeight());
			boolean chime = context.computeOnClient(mc ->
				mc.getSoundManager().getSoundEvent(HandicapContent.NEW_HANDICAP_SOUND_ID) != null);
			if (variants != 6 || !chime) {
				throw new AssertionError("sounds: " + variants + " fart variants, new handicap sound loaded=" + chime);
			}
			world.getServer().runCommand("handicap give @a flatulence");
			world.getServer().runCommand("playsound handicap:flatulence player @a ~ ~ ~");
			context.waitTicks(20);
		}
	}

	private static float pitchAfterTurn(ClientGameTestContext context, double amount) {
		context.runOnClient(mc -> mc.player.turn(0.0, amount));
		context.waitTicks(2);
		return context.computeOnClient(mc -> mc.player.getXRot());
	}
}
