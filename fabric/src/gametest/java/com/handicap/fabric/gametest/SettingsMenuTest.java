package com.handicap.fabric.gametest;

import com.handicap.Handicap;
import com.handicap.HandicapManager;
import com.handicap.client.ClientState;
import com.handicap.client.HandicapListScreen;
import com.handicap.client.HandicapSettingsScreen;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;

public class SettingsMenuTest implements FabricClientGameTest {
	private static final UUID BUDDY = UUID.randomUUID();

	@Override
	public void runTest(ClientGameTestContext context) {
		context.setScreen(() -> new HandicapSettingsScreen(null));
		context.waitTicks(5);
		context.takeScreenshot("settings-no-world");
		context.setScreen(() -> null);
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksRender();
			world.getServer().runCommand("handicap settings timer off");
			context.waitTicks(30);
			context.setScreen(() -> new HandicapSettingsScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("settings-menu");

			context.runOnClient(mc -> HandicapSettingsScreen.send("disable:blind", 1));
			context.runOnClient(mc -> HandicapSettingsScreen.send("stacking", 0));
			context.waitTicks(30);
			check(world.getServer().computeOnServer(s -> HandicapManager.state().isDisabled(Handicap.BLIND)), "server disabled blind");
			check(context.computeOnClient(mc -> ClientState.disabled(Handicap.BLIND)), "client sees blind disabled");
			check(!context.computeOnClient(mc -> ClientState.stacking()), "client sees stacking off");
			context.runOnClient(mc -> HandicapSettingsScreen.send("permission", 0));
			context.waitTicks(30);
			check(world.getServer().computeOnServer(s -> com.handicap.HandicapConfig.permissionLevel() == 0), "server permission is Player");
			check(context.computeOnClient(mc -> ClientState.permissionPlayer()), "client sees permission Player");
			context.runOnClient(mc -> HandicapSettingsScreen.send("permission", 2));
			context.waitTicks(30);
			check(!context.computeOnClient(mc -> ClientState.permissionPlayer()), "client sees permission Operator");
			context.setScreen(() -> new HandicapListScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("settings-list");
			context.setScreen(() -> null);

			world.getServer().runCommand("handicap settings stacking true");
			world.getServer().runCommand("handicap give @a all");
			context.waitTicks(5);
			check(count(world, null) == Handicap.values().length - 1, "give all skips the disabled handicap");

			world.getServer().runOnServer(server -> {
				GameProfile profile = new GameProfile(BUDDY, "SyncBuddy");
				ServerPlayer buddy = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
				server.getPlayerList().placeNewPlayer(new ProsopagnosiaMultiplayerTest.FakeConnection(), buddy, CommonListenerCookie.createInitial(profile, false));
			});
			context.waitTicks(10);
			check(count(world, BUDDY) == 0, "buddy starts without handicaps");
			context.runOnClient(mc -> HandicapSettingsScreen.send("sync", 1));
			context.waitTicks(30);
			check(count(world, BUDDY) == Handicap.values().length - 1, "sync copies the handicaps to the buddy");
			world.getServer().runCommand("handicap clear @a[name=!SyncBuddy] mute");
			context.waitTicks(5);
			check(!world.getServer().computeOnServer(s -> HandicapManager.state().has(BUDDY, Handicap.MUTE)), "sync removes mute from the buddy");
			context.setScreen(() -> new HandicapSettingsScreen(null));
			context.waitTicks(5);
			context.takeScreenshot("settings-menu-after");
			context.setScreen(() -> null);
		}
	}

	private static int count(TestSingleplayerContext world, UUID id) {
		return world.getServer().computeOnServer(server -> HandicapManager.state()
			.get(id != null ? id : server.getPlayerList().getPlayers().get(0).getUUID()).size());
	}

	private static void check(boolean ok, String label) {
		if (!ok) {
			throw new AssertionError(label);
		}
	}
}
