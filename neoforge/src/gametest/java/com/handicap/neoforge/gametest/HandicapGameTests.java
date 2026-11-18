package com.handicap.neoforge.gametest;

import com.handicap.Handicap;
import com.handicap.HandicapContent;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.HandicapState;
import com.handicap.WheelchairEntity;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HandicapMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HandicapGameTests {
	private static final String EMPTY = "empty";

	private static ServerPlayer reset(GameTestHelper helper) {
		HandicapState state = HandicapManager.state();
		if (state == null) {
			helper.fail("Handicap state is not loaded");
		}
		state.setTimerEnabled(false);
		state.setStacking(true);
		state.setMaxHandicaps(0);
		state.setDurationSeconds(0);
		state.setSync(false);
		for (Handicap d : Handicap.values()) {
			state.setDisabled(d, false);
		}
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		return player;
	}

	private static void run(GameTestHelper helper, String command) {
		MinecraftServer server = helper.getLevel().getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
	}

	private static int count(ServerPlayer player) {
		return HandicapManager.state().get(player.getUUID()).size();
	}

	private static void expect(GameTestHelper helper, ServerPlayer player, int expected, String label) {
		int actual = count(player);
		helper.assertTrue(actual == expected, label + ": expected " + expected + " handicaps, got " + actual);
	}

	@GameTest(template = EMPTY, batch = "handicap_all", timeoutTicks = 400)
	public static void everyHandicapRuns(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		player.getAbilities().invulnerable = true;
		run(helper, "execute as " + player.getStringUUID() + " run handicap give @s all");
		expect(helper, player, Handicap.values().length, "give all");
		helper.runAfterDelay(200, () -> {
			run(helper, "execute as " + player.getStringUUID() + " run handicap clear @s");
			expect(helper, player, 0, "clear after 10 s with every handicap");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, batch = "handicap_max")
	public static void maxAndStacking(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		HandicapState state = HandicapManager.state();
		state.setMaxHandicaps(3);
		for (int i = 0; i < 6; i++) {
			HandicapManager.giveRandom(player);
		}
		expect(helper, player, 3, "max 3 after 6 random handicaps");
		run(helper, "execute as " + player.getStringUUID() + " run handicap give @s all");
		expect(helper, player, Handicap.values().length, "give all ignores max");
		run(helper, "execute as " + player.getStringUUID() + " run handicap clear @s");
		state.setMaxHandicaps(0);
		HandicapManager.giveRandom(player);
		HandicapManager.giveRandom(player);
		state.setStacking(false);
		HandicapManager.giveRandom(player);
		expect(helper, player, 1, "a new handicap with stacking off");
		state.setStacking(true);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_duration", timeoutTicks = 300)
	public static void durationWearsOff(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		HandicapManager.state().setDurationSeconds(6);
		HandicapManager.give(player, Handicap.COLOR_BLIND, true);
		HandicapManager.give(player, Handicap.MYOPIA, true);
		expect(helper, player, 2, "two handicaps before they wear off");
		helper.runAfterDelay(200, () -> {
			expect(helper, player, 0, "6 s duration");
			HandicapManager.state().setDurationSeconds(0);
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, batch = "handicap_disabled")
	public static void disabledNeverGiven(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		HandicapState state = HandicapManager.state();
		state.setDisabled(Handicap.BLIND, true);
		run(helper, "execute as " + player.getStringUUID() + " run handicap give @s all");
		helper.assertFalse(state.has(player.getUUID(), Handicap.BLIND), "a disabled handicap was given by give all");
		expect(helper, player, Handicap.values().length - 1, "give all with one disabled");
		helper.assertFalse(HandicapManager.give(player, Handicap.BLIND, true), "a disabled handicap was given directly");
		state.setDisabled(Handicap.BLIND, false);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_sync")
	public static void syncSharesHandicaps(GameTestHelper helper) {
		ServerPlayer first = reset(helper);
		ServerPlayer second = helper.makeMockServerPlayerInLevel();
		HandicapState state = HandicapManager.state();
		state.setSync(true);
		HandicapManager.give(first, Handicap.COLOR_BLIND, true);
		helper.assertTrue(state.has(second.getUUID(), Handicap.COLOR_BLIND), "sync did not give the handicap to the other player");
		HandicapManager.remove(first, Handicap.COLOR_BLIND, true);
		helper.assertFalse(state.has(second.getUUID(), Handicap.COLOR_BLIND), "sync did not remove the handicap from the other player");
		state.setSync(false);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_mute")
	public static void muteBlocksChat(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		helper.assertTrue(HandicapManager.allowChat(player), "chat blocked without mute");
		HandicapManager.give(player, Handicap.MUTE, false);
		helper.assertFalse(HandicapManager.allowChat(player), "chat allowed with mute");
		HandicapManager.remove(player, Handicap.MUTE, false);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_wheelchair")
	public static void wheelchairRideAndBreak(GameTestHelper helper) {
		ServerPlayer player = reset(helper);
		BlockPos pos = new BlockPos(2, 1, 2);
		WheelchairEntity chair = helper.spawn(HandicapContent.wheelchair(), pos);
		player.teleportTo(chair.getX(), chair.getY(), chair.getZ());
		helper.assertTrue(player.startRiding(chair, true), "the player could not sit in the wheelchair");
		helper.assertTrue(player.getVehicle() == chair, "the player is not riding the wheelchair");
		chair.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0f);
		helper.assertTrue(chair.isRemoved(), "the wheelchair did not break in one hit");
		helper.assertTrue(player.getVehicle() == null, "the player is still riding a broken wheelchair");
		helper.assertItemEntityPresent(HandicapContent.wheelchairItem(), pos, 2.0);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_recipe")
	public static void wheelchairRecipeLoaded(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		Optional<RecipeHolder<?>> recipe = server.getRecipeManager().byKey(HandicapMod.id("wheelchair"));
		helper.assertTrue(recipe.isPresent(), "the wheelchair recipe is not loaded");
		helper.assertTrue(recipe.get().value().getResultItem(server.registryAccess()).is(HandicapContent.wheelchairItem()),
			"the wheelchair recipe does not make a wheelchair");
		helper.succeed();
	}
}
