package com.handicap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

public final class HandicapState {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final class Data {
		boolean timerEnabled = true;
		boolean stacking = true;
		int timerSeconds = 600;
		int ticksRemaining = 600 * 20;
		Map<String, List<String>> players = new HashMap<>();
		Map<String, Integer> cancerStage = new HashMap<>();
		Map<String, Map<String, String>> armor = new HashMap<>();
		Map<String, Integer> legSide = new HashMap<>();
		int maxHandicaps = 0;
		int durationSeconds = 0;
		Map<String, Map<String, Integer>> ages = new HashMap<>();
	}

	private final Path file;
	private boolean timerEnabled = true;
	private boolean stacking = true;
	private int timerSeconds = 600;
	private int ticksRemaining = 600 * 20;
	private final Map<UUID, EnumSet<Handicap>> players = new HashMap<>();
	private final Map<UUID, Integer> cancerStage = new HashMap<>();
	private final Map<UUID, Map<String, String>> armor = new HashMap<>();
	private final Map<UUID, Integer> legSide = new HashMap<>();
	private int maxHandicaps = 0;
	private int durationSeconds = 0;
	private final Map<UUID, Map<Handicap, Integer>> ages = new HashMap<>();

	private HandicapState(Path file) {
		this.file = file;
	}

	public static HandicapState load(MinecraftServer server) {
		Path file = server.getWorldPath(LevelResource.ROOT).resolve("handicap.json");
		HandicapState state = new HandicapState(file);
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				Data data = GSON.fromJson(reader, Data.class);
				if (data != null) {
					state.timerEnabled = data.timerEnabled;
					state.stacking = data.stacking;
					state.timerSeconds = Math.max(5, data.timerSeconds);
					state.ticksRemaining = Math.max(1, data.ticksRemaining);
					if (data.players != null) {
						for (Map.Entry<String, List<String>> e : data.players.entrySet()) {
							EnumSet<Handicap> set = EnumSet.noneOf(Handicap.class);
							for (String id : e.getValue()) {
								Handicap d = Handicap.byId(id);
								if (d != null) {
									set.add(d);
								}
							}
							state.players.put(UUID.fromString(e.getKey()), set);
						}
					}
					if (data.legSide != null) {
						for (Map.Entry<String, Integer> e : data.legSide.entrySet()) {
							state.legSide.put(UUID.fromString(e.getKey()), e.getValue());
						}
					}
					if (data.armor != null) {
						for (Map.Entry<String, Map<String, String>> e : data.armor.entrySet()) {
							state.armor.put(UUID.fromString(e.getKey()), new HashMap<>(e.getValue()));
						}
					}
					state.maxHandicaps = Math.max(0, data.maxHandicaps);
					state.durationSeconds = Math.max(0, data.durationSeconds);
					if (data.ages != null) {
						for (Map.Entry<String, Map<String, Integer>> e : data.ages.entrySet()) {
							Map<Handicap, Integer> map = new EnumMap<>(Handicap.class);
							for (Map.Entry<String, Integer> a : e.getValue().entrySet()) {
								Handicap d = Handicap.byId(a.getKey());
								if (d != null) {
									map.put(d, a.getValue());
								}
							}
							state.ages.put(UUID.fromString(e.getKey()), map);
						}
					}
					if (data.cancerStage != null) {
						for (Map.Entry<String, Integer> e : data.cancerStage.entrySet()) {
							state.cancerStage.put(UUID.fromString(e.getKey()), e.getValue());
						}
					}
				}
			} catch (Exception e) {
				HandicapMod.LOGGER.error("Could not read {}", file, e);
			}
		} else {
			state.ticksRemaining = state.timerSeconds * 20;
		}
		return state;
	}

	public void save() {
		Data data = new Data();
		data.timerEnabled = timerEnabled;
		data.stacking = stacking;
		data.timerSeconds = timerSeconds;
		data.ticksRemaining = ticksRemaining;
		for (Map.Entry<UUID, EnumSet<Handicap>> e : players.entrySet()) {
			List<String> ids = new ArrayList<>();
			for (Handicap d : e.getValue()) {
				ids.add(d.id);
			}
			data.players.put(e.getKey().toString(), ids);
		}
		for (Map.Entry<UUID, Integer> e : legSide.entrySet()) {
			data.legSide.put(e.getKey().toString(), e.getValue());
		}
		for (Map.Entry<UUID, Map<String, String>> e : armor.entrySet()) {
			data.armor.put(e.getKey().toString(), new HashMap<>(e.getValue()));
		}
		for (Map.Entry<UUID, Integer> e : cancerStage.entrySet()) {
			data.cancerStage.put(e.getKey().toString(), e.getValue());
		}
		data.maxHandicaps = maxHandicaps;
		data.durationSeconds = durationSeconds;
		for (Map.Entry<UUID, Map<Handicap, Integer>> e : ages.entrySet()) {
			Map<String, Integer> map = new HashMap<>();
			for (Map.Entry<Handicap, Integer> a : e.getValue().entrySet()) {
				map.put(a.getKey().id, a.getValue());
			}
			data.ages.put(e.getKey().toString(), map);
		}
		try {
			Files.writeString(file, GSON.toJson(data));
		} catch (IOException e) {
			HandicapMod.LOGGER.error("Could not write {}", file, e);
		}
	}

	public EnumSet<Handicap> get(UUID id) {
		EnumSet<Handicap> set = players.get(id);
		return set == null ? EnumSet.noneOf(Handicap.class) : EnumSet.copyOf(set);
	}

	public boolean has(UUID id, Handicap d) {
		EnumSet<Handicap> set = players.get(id);
		return set != null && set.contains(d);
	}

	public boolean give(UUID id, Handicap d) {
		boolean added = players.computeIfAbsent(id, k -> EnumSet.noneOf(Handicap.class)).add(d);
		if (added) {
			ages.computeIfAbsent(id, k -> new EnumMap<>(Handicap.class)).put(d, 0);
		}
		return added;
	}

	public boolean remove(UUID id, Handicap d) {
		Map<Handicap, Integer> map = ages.get(id);
		if (map != null) {
			map.remove(d);
		}
		EnumSet<Handicap> set = players.get(id);
		return set != null && set.remove(d);
	}

	public int age(UUID id, Handicap d) {
		Map<Handicap, Integer> map = ages.get(id);
		return map == null ? 0 : map.getOrDefault(d, 0);
	}

	public void ageAll(UUID id) {
		EnumSet<Handicap> set = players.get(id);
		if (set == null || set.isEmpty()) {
			return;
		}
		Map<Handicap, Integer> map = ages.computeIfAbsent(id, k -> new EnumMap<>(Handicap.class));
		for (Handicap d : set) {
			map.merge(d, 1, Integer::sum);
		}
	}

	public Handicap oldest(UUID id, Handicap except) {
		Handicap found = null;
		int best = -1;
		for (Handicap d : get(id)) {
			int a = age(id, d);
			if (d != except && a > best) {
				best = a;
				found = d;
			}
		}
		return found;
	}

	public int maxHandicaps() {
		return maxHandicaps;
	}

	public void setMaxHandicaps(int max) {
		this.maxHandicaps = Math.max(0, max);
	}

	public int durationSeconds() {
		return durationSeconds;
	}

	public void setDurationSeconds(int seconds) {
		this.durationSeconds = Math.max(0, seconds);
	}

	public void resetAll() {
		players.clear();
		ages.clear();
		cancerStage.clear();
		resetTimer();
	}

	public void rememberArmor(UUID id, String slot, String itemId) {
		armor.computeIfAbsent(id, k -> new HashMap<>()).put(slot, itemId);
	}

	public Map<String, String> armorMemory(UUID id) {
		Map<String, String> memory = armor.get(id);
		return memory == null ? new HashMap<>() : new HashMap<>(memory);
	}

	public void clearArmorMemory(UUID id) {
		armor.remove(id);
	}

	public int legSide(UUID id) {
		return legSide.getOrDefault(id, 0);
	}

	public void setLegSide(UUID id, int side) {
		legSide.put(id, side);
	}

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

	public boolean stacking() {
		return stacking;
	}

	public void setStacking(boolean stacking) {
		this.stacking = stacking;
	}

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

	public int ticksRemaining() {
		return ticksRemaining;
	}

	public int secondsRemaining() {
		return ticksRemaining / 20;
	}

	public void resetTimer() {
		this.ticksRemaining = timerSeconds * 20;
	}

	public boolean tickTimer() {
		ticksRemaining--;
		if (ticksRemaining <= 0) {
			ticksRemaining = timerSeconds * 20;
			return true;
		}
		return false;
	}
}
