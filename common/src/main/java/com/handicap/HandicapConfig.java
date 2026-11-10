package com.handicap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class HandicapConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final List<String> HUD_POSITIONS = List.of(
		"top_left", "top_right", "middle_right", "bottom_right", "bottom_left", "middle_left");
	private static final int DEFAULT_HUD_POSITION = 2;

	private static final class Data {
		String info = "permissionLevel: 0 = everyone can use /handicap, 2 = operators only. "
			+ "Without operator permission (cheats off, level 0) a player can only target themselves by name "
			+ "(no @a, @s...), but can still manage the timer and stacking. "
			+ "showDescriptions: false hides the handicap descriptions shown when hovering a handicap in the inventory. "
			+ "coloredNames: true writes handicap names in their color instead of showing a colored dot. "
			+ "hudPosition: where the in-game list sits (" + String.join(", ", HUD_POSITIONS) + "). "
			+ "hudBackground: false removes the dark background behind the in-game list.";
		int permissionLevel = 2;
		boolean showDescriptions = true;
		boolean coloredNames = true;
		String hudPosition = HUD_POSITIONS.get(DEFAULT_HUD_POSITION);
		boolean hudBackground = true;
		boolean hudShown = true;
	}

	private static int permissionLevel = 2;
	private static boolean showDescriptions = true;
	private static boolean coloredNames = true;
	private static int hudPosition = DEFAULT_HUD_POSITION;
	private static boolean hudBackground = true;
	private static boolean hudShown = true;
	private static Path file;

	private HandicapConfig() {
	}

	public static void load(Path directory) {
		file = directory.resolve("handicap.json");
		try {
			if (Files.exists(file)) {
				try (Reader reader = Files.newBufferedReader(file)) {
					Data data = GSON.fromJson(reader, Data.class);
					if (data != null) {
						permissionLevel = Math.max(0, Math.min(4, data.permissionLevel));
						showDescriptions = data.showDescriptions;
						coloredNames = data.coloredNames;
						int index = HUD_POSITIONS.indexOf(data.hudPosition);
						hudPosition = index < 0 ? DEFAULT_HUD_POSITION : index;
						hudBackground = data.hudBackground;
						hudShown = data.hudShown;
					}
				}
			} else {
				Files.createDirectories(directory);
				Files.writeString(file, GSON.toJson(new Data()));
			}
		} catch (Exception e) {
			HandicapMod.LOGGER.error("Could not read {}", file, e);
		}
	}

	public static void setPermissionLevel(int level) {
		permissionLevel = Math.max(0, Math.min(4, level));
	}

	public static void setShowDescriptions(boolean value) {
		showDescriptions = value;
	}

	public static void setColoredNames(boolean value) {
		coloredNames = value;
	}

	public static void cycleHudPosition(int step) {
		hudPosition = Math.floorMod(hudPosition + step, HUD_POSITIONS.size());
	}

	public static void setHudBackground(boolean value) {
		hudBackground = value;
	}

	public static void save() {
		if (file == null) {
			return;
		}
		try {
			Data data = new Data();
			data.permissionLevel = permissionLevel;
			data.showDescriptions = showDescriptions;
			data.coloredNames = coloredNames;
			data.hudPosition = HUD_POSITIONS.get(hudPosition);
			data.hudBackground = hudBackground;
			data.hudShown = hudShown;
			Files.writeString(file, GSON.toJson(data));
		} catch (Exception e) {
			HandicapMod.LOGGER.error("Could not write {}", file, e);
		}
	}

	public static int permissionLevel() {
		return permissionLevel;
	}

	public static boolean showDescriptions() {
		return showDescriptions;
	}

	public static boolean coloredNames() {
		return coloredNames;
	}

	public static String hudPosition() {
		return HUD_POSITIONS.get(hudPosition);
	}

	public static boolean hudShown() {
		return hudShown;
	}

	public static void setHudShown(boolean value) {
		hudShown = value;
	}

	public static boolean hudBackground() {
		return hudBackground;
	}

	public static boolean allows(CommandSourceStack source) {
		ServerPlayer player = source.getPlayer();
		if (player != null && source.getServer().isSingleplayerOwner(player.nameAndId())) {
			return true;
		}
		return switch (permissionLevel) {
			case 0 -> true;
			case 1 -> Commands.hasPermission(Commands.LEVEL_MODERATORS).test(source);
			case 2 -> Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
			case 3 -> Commands.hasPermission(Commands.LEVEL_ADMINS).test(source);
			default -> Commands.hasPermission(Commands.LEVEL_OWNERS).test(source);
		};
	}
}
