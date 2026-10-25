package com.handicap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class HandicapConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final class Data {
		String info = "permissionLevel: 0 = everyone can use /handicap, 2 = operators only. "
			+ "Without operator permission (cheats off, level 0) a player can only target themselves by name "
			+ "(no @a, @s...), but can still manage the timer and stacking. "
			+ "showDescriptions: false hides the handicap description when you receive one.";
		int permissionLevel = 2;
		boolean showDescriptions = true;
	}

	private static int permissionLevel = 2;
	private static boolean showDescriptions = true;
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

	public static void save() {
		if (file == null) {
			return;
		}
		try {
			Data data = new Data();
			data.permissionLevel = permissionLevel;
			data.showDescriptions = showDescriptions;
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

	public static boolean allows(CommandSourceStack source) {
		return switch (permissionLevel) {
			case 0 -> true;
			case 1 -> Commands.hasPermission(Commands.LEVEL_MODERATORS).test(source);
			case 2 -> Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(source);
			case 3 -> Commands.hasPermission(Commands.LEVEL_ADMINS).test(source);
			default -> Commands.hasPermission(Commands.LEVEL_OWNERS).test(source);
		};
	}
}
