package com.handicap.fabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class PermissionTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(s -> s.setAllowCommands(false)).create()) {
			context.waitTicks(20);
			boolean visible = context.computeOnClient(mc -> {
				var root = mc.player.connection.getCommands().getRoot().getChild("handicap");
				return root != null && root.getChild("next") != null;
			});
			if (!visible) {
				throw new AssertionError("the singleplayer host can't use /handicap next with cheats off");
			}
			context.runOnClient(mc -> mc.player.connection.sendCommand("handicap next " + mc.player.getGameProfile().name()));
			context.waitTicks(40);
			int count = world.getServer().computeOnServer(server ->
				com.handicap.HandicapManager.state().get(server.getPlayerList().getPlayers().get(0).getUUID()).size());
			if (count != 1) {
				throw new AssertionError("next with cheats off gave " + count + " handicaps instead of 1");
			}
		}
	}
}
