package com.handicap.neoforge.gametest;

import com.handicap.HandicapMod;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.server.IntegratedServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = HandicapMod.MOD_ID, value = Dist.CLIENT)
public final class ClientSmokeTest {
	private static final List<String> ALL_STEPS = List.of(
		"color_blind", "myopia", "hyperopia", "tunnel_vision", "inverted_vision", "visual_agnosia",
		"derealization", "migraine", "scoliosis", "blind", "double_vision");
	private static final String ONLY = System.getProperty("handicap.smoke.steps", "");
	private static final List<String> STEPS = ONLY.isBlank() ? ALL_STEPS : List.of(ONLY.split(","));
	private static final int STEP = 60;
	private static int ticks;

	private ClientSmokeTest() {
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("handicap.smoke")) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.getSingleplayerServer() == null) {
			return;
		}
		ticks++;
		int start = 60;
		if (ticks == start) {
			run(mc, "handicap settings timer off");
			run(mc, "handicap clear @a");
			run(mc, "time set noon");
			run(mc, "weather clear");
			run(mc, "execute as @a at @s positioned over world_surface run tp @s ~ ~ ~ 0 0");
		}
		if (ticks == start + 40) {
			shot(mc, "baseline");
		}
		for (int i = 0; i < STEPS.size(); i++) {
			int at = start + STEP * (i + 1);
			if (ticks == at) {
				run(mc, "handicap clear @a");
				run(mc, "handicap give @a " + STEPS.get(i));
			} else if (ticks == at + 45) {
				shot(mc, STEPS.get(i));
			}
		}
		int end = start + STEP * (STEPS.size() + 1);
		if (!ONLY.isBlank()) {
			if (ticks == end) {
				run(mc, "handicap clear @a");
				HandicapMod.LOGGER.info("[Handicap smoke] finished");
			} else if (ticks == end + 20) {
				mc.stop();
			}
			return;
		}
		if (ticks == end) {
			run(mc, "handicap clear @a");
			run(mc, "handicap give @a all");
		} else if (ticks == end + 200) {
			shot(mc, "all");
			run(mc, "handicap clear @a");
			HandicapMod.LOGGER.info("[Handicap smoke] finished");
		} else if (ticks == end + 220) {
			mc.stop();
		}
	}

	private static void run(Minecraft mc, String command) {
		IntegratedServer server = mc.getSingleplayerServer();
		server.execute(() -> server.getCommands().performPrefixedCommand(
			server.createCommandSourceStack().withSuppressedOutput(), command));
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, "smoke-" + name + ".png", mc.getMainRenderTarget(),
			message -> HandicapMod.LOGGER.info("[Handicap smoke] {}", message.getString()));
	}
}
