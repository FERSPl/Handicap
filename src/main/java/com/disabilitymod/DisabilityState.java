package com.disabilitymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/** Données sauvegardées dans le dossier du monde (disabilitymod.json). */
public final class DisabilityState {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final class Data {
		boolean timerEnabled = true;
		int timerSeconds = 600;
		int ticksRemaining = 600 * 20;
		Map<String, List<String>> players = new HashMap<>();
		Map<String, Integer> cancerStage = new HashMap<>();
	}

	private final Path file;
	private boolean timerEnabled = true;
	private int timerSeconds = 600;
	private int ticksRemaining = 600 * 20;
	private final Map<UUID, EnumSet<Disability>> players = new HashMap<>();
	private final Map<UUID, Integer> cancerStage = new HashMap<>();

	private DisabilityState(Path file) {
		this.file = file;
	}

	public static DisabilityState load(MinecraftServer server) {
		Path file = server.getWorldPath(LevelResource.ROOT).resolve("disabilitymod.json");
		DisabilityState state = new DisabilityState(file);
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				Data data = GSON.fromJson(reader, Data.class);
				if (data != null) {
					state.timerEnabled = data.timerEnabled;
					state.timerSeconds = Math.max(5, data.timerSeconds);
					state.ticksRemaining = Math.max(1, data.ticksRemaining);
					if (data.players != null) {
						for (Map.Entry<String, List<String>> e : data.players.entrySet()) {
							EnumSet<Disability> set = EnumSet.noneOf(Disability.class);
							for (String id : e.getValue()) {
								Disability d = Disability.byId(id);
								if (d != null) {
									set.add(d);
								}
							}
							state.players.put(UUID.fromString(e.getKey()), set);
						}
					}
					if (data.cancerStage != null) {
						for (Map.Entry<String, Integer> e : data.cancerStage.entrySet()) {
							state.cancerStage.put(UUID.fromString(e.getKey()), e.getValue());
						}
					}
				}
			} catch (Exception e) {
				DisabilityMod.LOGGER.error("Impossible de lire {}", file, e);
			}
		} else {
			state.ticksRemaining = state.timerSeconds * 20;
		}
		return state;
	}

	public void save() {
		Data data = new Data();
		data.timerEnabled = timerEnabled;
		data.timerSeconds = timerSeconds;
		data.ticksRemaining = ticksRemaining;
		for (Map.Entry<UUID, EnumSet<Disability>> e : players.entrySet()) {
			List<String> ids = new ArrayList<>();
			for (Disability d : e.getValue()) {
				ids.add(d.id);
			}
			data.players.put(e.getKey().toString(), ids);
		}
		for (Map.Entry<UUID, Integer> e : cancerStage.entrySet()) {
			data.cancerStage.put(e.getKey().toString(), e.getValue());
		}
		try {
			Files.writeString(file, GSON.toJson(data));
		} catch (IOException e) {
			DisabilityMod.LOGGER.error("Impossible d'écrire {}", file, e);
		}
	}

	// --- Handicaps ---

	public EnumSet<Disability> get(UUID id) {
		EnumSet<Disability> set = players.get(id);
		return set == null ? EnumSet.noneOf(Disability.class) : EnumSet.copyOf(set);
	}

	public boolean has(UUID id, Disability d) {
		EnumSet<Disability> set = players.get(id);
		return set != null && set.contains(d);
	}

	public boolean give(UUID id, Disability d) {
		return players.computeIfAbsent(id, k -> EnumSet.noneOf(Disability.class)).add(d);
	}

	public boolean remove(UUID id, Disability d) {
		EnumSet<Disability> set = players.get(id);
		return set != null && set.remove(d);
	}

	public void resetAll() {
		players.clear();
		cancerStage.clear();
		resetTimer();
	}

	// --- Cancer ---

	public int cancerStage(UUID id) {
		return cancerStage.getOrDefault(id, 0);
	}

	public void setCancerStage(UUID id, int stage) {
		if (stage <= 0) {
			cancerStage.remove(id);
		} else {
			cancerStage.put(id, stage);
		}
	}

	// --- Timer ---

	public boolean timerEnabled() {
		return timerEnabled;
	}

	public void setTimerEnabled(boolean enabled) {
		this.timerEnabled = enabled;
	}

	public int timerSeconds() {
		return timerSeconds;
	}

	public void setTimerSeconds(int seconds) {
		this.timerSeconds = seconds;
		this.timerEnabled = true;
		resetTimer();
	}

	public int secondsRemaining() {
		return ticksRemaining / 20;
	}

	public void resetTimer() {
		this.ticksRemaining = timerSeconds * 20;
	}

	/** Fait avancer le timer d'un tick. Renvoie true quand il arrive à zéro (et le relance). */
	public boolean tickTimer() {
		ticksRemaining--;
		if (ticksRemaining <= 0) {
			ticksRemaining = timerSeconds * 20;
			return true;
		}
		return false;
	}
}
