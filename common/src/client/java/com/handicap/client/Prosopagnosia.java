package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.client.mixin.WalkAnimationAccessor;

import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

public final class Prosopagnosia {
	private static final String HIDDEN_NAME = "???";
	private static final UUID STEVE = new UUID(0L, 0L);
	private static final EntityType<?>[] DISGUISES = {
		EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.HUSK,
		EntityType.STRAY, EntityType.WITCH, EntityType.PILLAGER, EntityType.DROWNED
	};

	private static final Map<Integer, Entity> FAKES = new HashMap<>();
	private static ClientLevel fakeLevel;
	private static List<String> names = List.of();

	private Prosopagnosia() {
	}

	public static boolean active() {
		return ClientState.has(Handicap.PROSOPAGNOSIA);
	}

	public static boolean hides(UUID id) {
		Minecraft mc = Minecraft.getInstance();
		return active() && mc.player != null && !mc.player.getUUID().equals(id);
	}

	public static void tick(Minecraft mc) {
		if (!active() || mc.level == null || mc.getConnection() == null) {
			FAKES.clear();
			names = List.of();
			return;
		}
		FAKES.keySet().removeIf(id -> mc.level.getEntity(id) == null);
		List<String> found = new ArrayList<>();
		for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
			String name = info.getProfile().name();
			if (mc.player != null && !info.getProfile().id().equals(mc.player.getUUID()) && name.length() >= 2) {
				found.add(name);
			}
		}
		names = found;
	}

	public static String hideNames(String text) {
		List<String> current = names;
		if (text == null || current.isEmpty() || !active()) {
			return text;
		}
		String out = text;
		for (String name : current) {
			if (out.contains(name)) {
				out = out.replace(name, HIDDEN_NAME);
			}
		}
		return out;
	}

	public static Entity disguise(Entity entity) {
		if (!active() || !(entity.level() instanceof ClientLevel level)) {
			return entity;
		}
		boolean villager = entity instanceof AbstractVillager;
		EntityType<?> target = villager ? EntityType.PLAYER : monsterDisguise(entity);
		if (target == null) {
			return entity;
		}
		if (level != fakeLevel) {
			FAKES.clear();
			fakeLevel = level;
		}
		Entity fake = FAKES.get(entity.getId());
		if (fake == null || fake.getType() != target) {
			fake = create(level, target);
			if (fake == null) {
				return entity;
			}
			fake.setId(entity.getId());
			FAKES.put(entity.getId(), fake);
		}
		sync(entity, fake);
		return fake;
	}

	private static EntityType<?> monsterDisguise(Entity entity) {
		if (!(entity instanceof Monster) || entity.getBbHeight() < 0.5f || entity.getBbHeight() > 2.2f) {
			return null;
		}
		int index = Math.floorMod(entity.getUUID().hashCode(), DISGUISES.length);
		if (DISGUISES[index] == entity.getType()) {
			index = (index + 1) % DISGUISES.length;
		}
		return DISGUISES[index];
	}

	private static Entity create(ClientLevel level, EntityType<?> type) {
		try {
			if (type == EntityType.PLAYER) {
				return new RemotePlayer(level, new GameProfile(STEVE, ""));
			}
			return type.create(level, EntitySpawnReason.LOAD);
		} catch (Throwable t) {
			return null;
		}
	}

	private static void sync(Entity from, Entity to) {
		to.setPos(from.getX(), from.getY(), from.getZ());
		to.xo = from.xo;
		to.yo = from.yo;
		to.zo = from.zo;
		to.xOld = from.xOld;
		to.yOld = from.yOld;
		to.zOld = from.zOld;
		to.setYRot(from.getYRot());
		to.setXRot(from.getXRot());
		to.yRotO = from.yRotO;
		to.xRotO = from.xRotO;
		to.tickCount = from.tickCount;
		to.setOnGround(from.onGround());
		to.setPose(from.getPose());
		to.setInvisible(from.isInvisible());
		to.setRemainingFireTicks(from.getRemainingFireTicks());
		if (from instanceof LivingEntity src && to instanceof LivingEntity dst) {
			dst.yBodyRot = src.yBodyRot;
			dst.yBodyRotO = src.yBodyRotO;
			dst.yHeadRot = src.yHeadRot;
			dst.yHeadRotO = src.yHeadRotO;
			dst.hurtTime = src.hurtTime;
			dst.deathTime = src.deathTime;
			WalkAnimationAccessor a = (WalkAnimationAccessor) src.walkAnimation;
			WalkAnimationAccessor b = (WalkAnimationAccessor) dst.walkAnimation;
			b.handicap$setSpeedOld(a.handicap$getSpeedOld());
			b.handicap$setSpeed(a.handicap$getSpeed());
			b.handicap$setPosition(a.handicap$getPosition());
			if (dst instanceof Mob mob && mob.isBaby() != src.isBaby()) {
				mob.setBaby(src.isBaby());
			}
		}
	}
}
