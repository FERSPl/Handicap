package com.handicap.forge.gametest;

import com.handicap.Handicap;
import com.handicap.HandicapContent;
import com.handicap.HandicapManager;
import com.handicap.HandicapMod;
import com.handicap.HandicapState;
import com.handicap.WheelchairEntity;

import com.mojang.authlib.GameProfile;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HandicapMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HandicapGameTests {
	private static final String EMPTY = "empty";
	private static int players;

	@SuppressWarnings("unchecked")
	private static ServerPlayer player(GameTestHelper helper) {
		ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "htest" + players++));
		try {
			PlayerList list = helper.getLevel().getServer().getPlayerList();
			Field players = PlayerList.class.getDeclaredField("players");
			players.setAccessible(true);
			((List<ServerPlayer>) players.get(list)).add(player);
			Field byUuid = PlayerList.class.getDeclaredField("playersByUUID");
			byUuid.setAccessible(true);
			((Map<UUID, ServerPlayer>) byUuid.get(list)).put(player.getUUID(), player);
		} catch (ReflectiveOperationException e) {
			helper.fail("Could not add the test player: " + e);
		}
		return player;
	}

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
		ServerPlayer player = player(helper);
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
		run(helper, "handicap give " + player.getGameProfile().getName() + " all");
		expect(helper, player, Handicap.values().length, "give all");
		helper.runAfterDelay(200, () -> {
			run(helper, "handicap clear " + player.getGameProfile().getName());
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
		run(helper, "handicap give " + player.getGameProfile().getName() + " all");
		expect(helper, player, Handicap.values().length, "give all ignores max");
		run(helper, "handicap clear " + player.getGameProfile().getName());
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
		run(helper, "handicap give " + player.getGameProfile().getName() + " all");
		helper.assertFalse(state.has(player.getUUID(), Handicap.BLIND), "a disabled handicap was given by give all");
		expect(helper, player, Handicap.values().length - 1, "give all with one disabled");
		helper.assertFalse(HandicapManager.give(player, Handicap.BLIND, true), "a disabled handicap was given directly");
		state.setDisabled(Handicap.BLIND, false);
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_sync")
	public static void syncSharesHandicaps(GameTestHelper helper) {
		ServerPlayer first = reset(helper);
		ServerPlayer second = player(helper);
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

	@GameTest(template = EMPTY, batch = "handicap_sounds")
	public static void newSoundsRegistered(GameTestHelper helper) {
		helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.containsKey(HandicapContent.FLATULENCE_SOUND_ID),
			"the flatulence sound is not registered");
		helper.assertTrue(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.containsKey(HandicapContent.NEW_HANDICAP_SOUND_ID),
			"the new handicap sound is not registered");
		helper.succeed();
	}

	@GameTest(template = EMPTY, batch = "handicap_recipe")
	public static void wheelchairRecipeLoaded(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		Optional<? extends Recipe<?>> recipe = server.getRecipeManager().byKey(HandicapMod.id("wheelchair"));
		helper.assertTrue(recipe.isPresent(), "the wheelchair recipe is not loaded");
		helper.assertTrue(recipe.get().getResultItem(server.registryAccess()).is(HandicapContent.wheelchairItem()),
			"the wheelchair recipe does not make a wheelchair");
		helper.succeed();
	}
}
