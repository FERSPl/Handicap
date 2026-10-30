package com.handicap.fabric.gametest;

import com.handicap.client.Prosopagnosia;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

public class ProsopagnosiaVisualTest implements FabricClientGameTest {
	private static final String[] MOBS = {
		"villager ^-3 ^ ^5", "villager ^-1.5 ^ ^5", "zombie ^0 ^ ^5", "creeper ^1.5 ^ ^5", "skeleton ^3 ^ ^5",
		"spider ^-2 ^ ^8", "enderman ^2 ^ ^8"
	};

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getConnection().waitForChunksRender();
			world.getServer().runCommand("time set midnight");
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("effect give @a night_vision infinite 0 true");
			world.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 10");
			context.waitTicks(10);
			for (String mob : MOBS) {
				String[] parts = mob.split(" ", 2);
				world.getServer().runCommand("execute as @p at @s run summon minecraft:" + parts[0] + " " + parts[1]
					+ " {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
			}
			context.waitTicks(40);
			context.takeScreenshot("prosopagnosia-before");

			world.getServer().runCommand("handicap give @a prosopagnosia");
			context.waitTicks(40);
			context.takeScreenshot("prosopagnosia-after");
			String problem = context.computeOnClient(mc -> {
				for (Entity entity : mc.level.entitiesForRendering()) {
					Entity fake = Prosopagnosia.disguise(entity);
					if (entity instanceof Monster && entity.getBbHeight() <= 2.2f && fake == entity) {
						return entity.getType() + " is not disguised";
					}
					if (fake != entity && (fake.xOld != entity.xOld || fake.yOld != entity.yOld || fake.zOld != entity.zOld)) {
						return entity.getType() + " disguise is drawn at the wrong place between ticks";
					}
				}
				return null;
			});
			if (problem != null) {
				throw new AssertionError(problem);
			}
			for (int i = 0; i < 8; i++) {
				context.waitTicks(2);
				context.takeScreenshot("prosopagnosia-burst-" + i);
			}

			world.getServer().runCommand("handicap clear @a prosopagnosia");
			context.waitTicks(40);
			context.takeScreenshot("prosopagnosia-removed");
		}
	}
}
