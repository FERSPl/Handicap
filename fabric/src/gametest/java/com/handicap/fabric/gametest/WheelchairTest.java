package com.handicap.fabric.gametest;

import com.handicap.HandicapContent;
import com.handicap.WheelchairEntity;

import java.util.List;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Crafting recipe, placing, riding, climbing a 1-block step, and breaking the wheelchair. */
public class WheelchairTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("handicap clear @a");
			world.getServer().runCommand("gamemode survival @a");
			world.getServer().runCommand("time set noon");
			world.getServer().runCommand("fill -6 199 -6 6 199 20 stone");
			world.getServer().runCommand("fill -6 200 -6 6 210 20 air");
			world.getServer().runCommand("fill -6 200 6 6 200 20 stone");
			world.getServer().runCommand("tp @a 0 200 -3 0 0");
			context.waitTicks(20);

			boolean recipe = world.getServer().computeOnServer(server -> server.getRecipeManager()
				.byKey(ResourceKey.create(Registries.RECIPE, HandicapContent.WHEELCHAIR_ID)).isPresent());
			if (!recipe) {
				throw new AssertionError("the wheelchair recipe is not loaded");
			}

			// Place it with the item, like a player would.
			int left = world.getServer().computeOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(HandicapContent.wheelchairItem()));
				BlockPos floor = new BlockPos(0, 199, 0);
				HandicapContent.wheelchairItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
				return player.getMainHandItem().getCount();
			});
			if (left != 0) {
				throw new AssertionError("placing the wheelchair did not use the item");
			}
			context.waitTicks(10);

			// Sit in it.
			world.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				chair(server.overworld()).interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
			});
			context.waitTicks(10);
			boolean riding = context.computeOnClient(mc -> mc.player.getVehicle() instanceof WheelchairEntity);
			if (!riding) {
				throw new AssertionError("the player is not sitting in the wheelchair");
			}
			context.runOnClient(mc -> {
				mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
				mc.player.setXRot(40.0f);
			});
			context.waitTicks(10);
			context.takeScreenshot("wheelchair-riding");

			// Drive forward into the 1-block step at z = 6.
			context.runOnClient(mc -> mc.player.setXRot(0.0f));
			context.getInput().holdKey(options -> options.keyUp);
			context.waitTicks(50);
			context.getInput().releaseKey(options -> options.keyUp);
			context.waitTicks(20);
			Vec3 pos = world.getServer().computeOnServer(server -> chair(server.overworld()).position());
			if (pos.z < 7.0 || pos.y < 200.9) {
				throw new AssertionError("the wheelchair did not climb the step, it is at " + pos);
			}
			context.runOnClient(mc -> {
				mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
				mc.player.setXRot(40.0f);
			});
			context.waitTicks(10);
			context.takeScreenshot("wheelchair-back-above");
			context.runOnClient(mc -> mc.player.setXRot(5.0f));
			context.waitTicks(10);
			context.takeScreenshot("wheelchair-on-step");

			// Get off and break it: it drops its item.
			world.getServer().runOnServer(server -> {
				ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				player.stopRiding();
				ServerLevel level = server.overworld();
				chair(level).hurtServer(level, level.damageSources().playerAttack(player), 1.0f);
			});
			context.waitTicks(5);
			boolean dropped = world.getServer().computeOnServer(server -> {
				ServerLevel level = server.overworld();
				List<WheelchairEntity> chairs = level.getEntitiesOfClass(WheelchairEntity.class, area());
				List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area(),
					item -> item.getItem().is(HandicapContent.wheelchairItem()));
				return chairs.isEmpty() && !items.isEmpty();
			});
			if (!dropped) {
				throw new AssertionError("breaking the wheelchair did not drop its item");
			}
			context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
		}
	}

	private static AABB area() {
		return new AABB(-30, -100, -30, 30, 260, 60);
	}

	private static WheelchairEntity chair(ServerLevel level) {
		List<WheelchairEntity> chairs = level.getEntitiesOfClass(WheelchairEntity.class, area());
		if (chairs.size() != 1) {
			throw new AssertionError("expected 1 wheelchair, found " + chairs.size());
		}
		return chairs.get(0);
	}
}
