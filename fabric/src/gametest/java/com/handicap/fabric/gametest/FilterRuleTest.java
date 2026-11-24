package com.handicap.fabric.gametest;

import com.handicap.Handicap;
import com.handicap.HandicapManager;

import java.util.EnumSet;
import java.util.Set;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class FilterRuleTest implements FabricClientGameTest {
	private static final Set<Handicap> FILTERS = EnumSet.of(Handicap.COLOR_BLIND, Handicap.VISUAL_AGNOSIA, Handicap.MYOPIA,
		Handicap.HYPEROPIA, Handicap.TUNNEL_VISION, Handicap.INVERTED_VISION, Handicap.DEREALIZATION, Handicap.DOUBLE_VISION);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			world.getServer().runCommand("handicap settings timer off");
			world.getServer().runCommand("handicap give @a color_blind");
			for (int i = 0; i < 40; i++) {
				world.getServer().runCommand("handicap next @a");
				context.waitTicks(2);
			}
			Set<Handicap> owned = world.getServer().computeOnServer(server ->
				HandicapManager.state().get(server.getPlayerList().getPlayers().get(0).getUUID()));
			long filters = owned.stream().filter(FILTERS::contains).count();
			if (owned.size() != 41 || filters != 1) {
				throw new AssertionError(owned.size() + " handicaps, " + filters + " permanent filters");
			}

			world.getServer().runCommand("handicap settings max 1");
			boolean filterReplaced = false;
			for (int i = 0; i < 80 && !filterReplaced; i++) {
				world.getServer().runCommand("handicap clear @a");
				world.getServer().runCommand("handicap give @a color_blind");
				world.getServer().runCommand("handicap next @a");
				context.waitTicks(2);
				Set<Handicap> now = world.getServer().computeOnServer(server ->
					EnumSet.copyOf(HandicapManager.state().get(server.getPlayerList().getPlayers().get(0).getUUID())));
				filterReplaced = now.size() == 1 && FILTERS.contains(now.iterator().next()) && !now.contains(Handicap.COLOR_BLIND);
			}
			world.getServer().runCommand("handicap settings max off");
			if (!filterReplaced) {
				throw new AssertionError("with max 1, a filter was never drawn while the old filter was going away");
			}
		}
	}
}
