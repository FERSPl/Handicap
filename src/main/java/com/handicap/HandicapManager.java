package com.handicap;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HandicapManager {
	private static HandicapState state;
	private static MinecraftServer server;
	private static final Map<UUID, Runtime> RUNTIME = new HashMap<>();

	private static final String[] TOURETTE_WORDS = {
		"BEEP!", "YES YES YES", "POTATO!", "AAAAH", "no no no", "BOOM", "HEY!", "*squeak*"
	};

	private static final class Runtime {
		int cancerSeconds;
		int sprintSeconds;
		int windedSeconds;
		int narcoSeconds;
		boolean blindApplied;
		int glutenSeconds;
		int hypoSeconds;
		int idleSeconds;
		int numbSeconds;
		double lastX;
		double lastZ;
		int clumsyCooldown;
		int phobiaSeconds;
		int claustroSeconds;
		int reflexSeconds;

		ItemStack useItem;
		int useRemaining;

		float lastHealth = -1f;
		int seizureSeconds;

		int ocdType;
		int ocdRemaining;
		int ocdDeadline;
		int ocdCooldown = 30;
		boolean lastOnGround = true;
		boolean lastSneak;

		long lastMask = -1L;
		int lastFlags = -1;
	}

	private HandicapManager() {
	}

	public static HandicapState state() {
		return state;
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(s -> {
			server = s;
			state = HandicapState.load(s);
			RUNTIME.clear();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
			if (state != null) {
				state.save();
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
			state = null;
			server = null;
			RUNTIME.clear();
		});
		ServerTickEvents.END_SERVER_TICK.register(HandicapManager::onServerTick);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> RUNTIME.remove(handler.getPlayer().getUUID()));

		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			if (state != null && state.has(sender.getUUID(), Handicap.MUTE)) {
				sender.sendOverlayMessage(Component.literal("You are mute: you can't speak."));
				return false;
			}
			return true;
		});
	}

	public static boolean give(ServerPlayer p, Handicap d, boolean announce) {
		if (announce && !state.stacking()) {
			for (Handicap other : state.get(p.getUUID())) {
				if (other != d) {
					state.remove(p.getUUID(), other);
				}
			}
		}
		if (!state.give(p.getUUID(), d)) {
			return false;
		}
		if (announce && !state.has(p.getUUID(), Handicap.ANOSOGNOSIA)) {
			p.sendSystemMessage(Component.literal("New handicap: " + d.displayName + " — " + d.description)
				.withStyle(ChatFormatting.RED));
		}
		refresh(p);
		return true;
	}

	public static boolean remove(ServerPlayer p, Handicap d, boolean announce) {
		if (!state.remove(p.getUUID(), d)) {
			return false;
		}
		if (announce && !state.has(p.getUUID(), Handicap.ANOSOGNOSIA)) {
			p.sendSystemMessage(Component.literal("Handicap removed: " + d.displayName)
				.withStyle(ChatFormatting.GREEN));
		}
		refresh(p);
		return true;
	}

	public static Handicap giveRandom(ServerPlayer p) {
		List<Handicap> available = new ArrayList<>();
		for (Handicap d : Handicap.values()) {
			if (!state.has(p.getUUID(), d)) {
				available.add(d);
			}
		}
		if (available.isEmpty()) {
			p.sendSystemMessage(Component.literal("You already have every handicap!").withStyle(ChatFormatting.GRAY));
			return null;
		}
		Handicap d = available.get(p.getRandom().nextInt(available.size()));
		give(p, d, true);
		return d;
	}

	public static Handicap removeRandom(ServerPlayer p) {
		List<Handicap> owned = new ArrayList<>(state.get(p.getUUID()));
		if (owned.isEmpty()) {
			return null;
		}
		Handicap d = owned.get(p.getRandom().nextInt(owned.size()));
		remove(p, d, true);
		return d;
	}

	public static void refresh(ServerPlayer p) {
		if (state != null && server != null) {
			tickPlayer(p);
		}
	}

	private static void onServerTick(MinecraftServer s) {
		if (state == null) {
			return;
		}
		List<ServerPlayer> players = s.getPlayerList().getPlayers();
		if (players.isEmpty()) {
			return;
		}

		if (state.timerEnabled() && state.tickTimer()) {
			for (ServerPlayer p : new ArrayList<>(players)) {
				giveRandom(p);
			}
			state.save();
		}

		for (ServerPlayer p : new ArrayList<>(players)) {
			tickFast(p);
		}

		int tick = s.getTickCount();
		if (tick % 20 == 0) {
			for (ServerPlayer p : new ArrayList<>(players)) {
				tickPlayer(p);
			}
		}
		if (tick % 6000 == 0) {
			state.save();
		}
	}

	private static void tickPlayer(ServerPlayer p) {
		UUID id = p.getUUID();
		Set<Handicap> set = state.get(id);
		Runtime rt = RUNTIME.computeIfAbsent(id, k -> new Runtime());
		RandomSource rnd = p.getRandom();

		if (set.contains(Handicap.BLIND)) {
			MobEffectInstance cur = p.getEffect(MobEffects.BLINDNESS);
			if (cur == null || cur.getDuration() < 200) {
				p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0, false, false, false));
			}
			rt.blindApplied = true;
		} else if (rt.blindApplied) {
			p.removeEffect(MobEffects.BLINDNESS);
			rt.blindApplied = false;
		}

		boolean isShort = set.contains(Handicap.SHORT);
		setModifier(p, Attributes.SCALE, "short_scale", -0.28, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, isShort);
		setModifier(p, Attributes.BLOCK_INTERACTION_RANGE, "short_block_reach", -1.0, AttributeModifier.Operation.ADD_VALUE, isShort);
		setModifier(p, Attributes.ENTITY_INTERACTION_RANGE, "short_entity_reach", -0.75, AttributeModifier.Operation.ADD_VALUE, isShort);

		boolean arthritis = set.contains(Handicap.ARTHRITIS);
		setModifier(p, Attributes.BLOCK_BREAK_SPEED, "arthritis_mining", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, arthritis);
		setModifier(p, Attributes.ATTACK_SPEED, "arthritis_attack", -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, arthritis);

		setModifier(p, Attributes.FALL_DAMAGE_MULTIPLIER, "brittle_bones", 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			set.contains(Handicap.BRITTLE_BONES));

		int stage = state.cancerStage(id);
		if (set.contains(Handicap.CANCER)) {
			rt.cancerSeconds++;
			if (rt.cancerSeconds >= 60) {
				rt.cancerSeconds = 0;
				if (stage < 9) {
					stage++;
					state.setCancerStage(id, stage);
					p.sendOverlayMessage(Component.literal("Your health is getting worse..."));
				}
			}
		} else {
			rt.cancerSeconds = 0;
			if (stage != 0) {
				state.setCancerStage(id, 0);
				stage = 0;
			}
		}
		setModifier(p, Attributes.MAX_HEALTH, "cancer_health", -2.0 * stage, AttributeModifier.Operation.ADD_VALUE,
			set.contains(Handicap.CANCER) && stage > 0);

		if (!set.contains(Handicap.ASTHMA)) {
			rt.sprintSeconds = 0;
			rt.windedSeconds = 0;
		} else if (rt.windedSeconds > 0) {
			rt.windedSeconds--;
			p.setSprinting(false);
		} else if (p.isSprinting()) {
			rt.sprintSeconds++;
			if (rt.sprintSeconds >= 10) {
				rt.sprintSeconds = 0;
				rt.windedSeconds = 8;
				p.setSprinting(false);
				p.sendOverlayMessage(Component.literal("You're out of breath!"));
			}
		} else {
			rt.sprintSeconds = Math.max(0, rt.sprintSeconds - 2);
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "asthma_winded", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.windedSeconds > 0);

		if (!set.contains(Handicap.NARCOLEPSY)) {
			rt.narcoSeconds = 0;
		} else if (rt.narcoSeconds > 0) {
			rt.narcoSeconds--;
		} else if (rnd.nextInt(45) == 0) {
			rt.narcoSeconds = 8;
			p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 180, 0, false, false, false));
			p.sendOverlayMessage(Component.literal("You fall asleep... Zzz..."));
		}
		boolean asleep = rt.narcoSeconds > 0;
		setModifier(p, Attributes.MOVEMENT_SPEED, "narcolepsy_move", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, asleep);
		setModifier(p, Attributes.JUMP_STRENGTH, "narcolepsy_jump", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, asleep);

		if (set.contains(Handicap.INSOMNIA) && p.isSleeping()) {
			p.stopSleepInBed(true, true);
			p.sendOverlayMessage(Component.literal("You can't fall asleep."));
		}

		if (set.contains(Handicap.AMPUTATED_OFFHAND)) {
			ItemStack off = p.getOffhandItem();
			if (!off.isEmpty()) {
				ItemStack copy = off.copy();
				p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
				if (!p.getInventory().add(copy)) {
					p.drop(copy, false);
				}
			}
		}

		if (set.contains(Handicap.DEMENTIA) && rnd.nextInt(30) == 0) {
			int a = rnd.nextInt(36);
			int b = rnd.nextInt(36);
			if (a != b) {
				ItemStack ia = p.getInventory().getItem(a).copy();
				ItemStack ib = p.getInventory().getItem(b).copy();
				if (!ia.isEmpty() || !ib.isEmpty()) {
					p.getInventory().setItem(a, ib);
					p.getInventory().setItem(b, ia);
				}
			}
		}

		if (set.contains(Handicap.POLLEN_ALLERGY) && !p.hasEffect(MobEffects.POISON)) {
			BlockPos base = p.blockPosition();
			for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(2, 2, 2))) {
				if (p.level().getBlockState(pos).is(BlockTags.FLOWERS)) {
					p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
					p.sendOverlayMessage(Component.literal("Achoo! The pollen is poisoning you."));
					break;
				}
			}
		}

		if (set.contains(Handicap.TOURETTES) && rnd.nextInt(25) == 0) {
			String word = TOURETTE_WORDS[rnd.nextInt(TOURETTE_WORDS.length)];
			Component msg = Component.translatable("chat.type.text", p.getDisplayName(), Component.literal(word));
			server.getPlayerList().broadcastSystemMessage(msg, false);
		}

		tickPlayerExtra(p, set, rt);
	}

	private static void tickPlayerExtra(ServerPlayer p, Set<Handicap> set, Runtime rt) {
		RandomSource rnd = p.getRandom();

		tickMoreHandicaps(p, set, rt, rnd);

		setModifier(p, Attributes.JUMP_STRENGTH, "paraplegia_jump", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			set.contains(Handicap.PARAPLEGIA));
		setModifier(p, Attributes.MOVEMENT_SPEED, "reduced_mobility", -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			set.contains(Handicap.REDUCED_MOBILITY));
		if (!set.contains(Handicap.CELIAC)) {
			rt.glutenSeconds = 0;
		} else if (rt.glutenSeconds > 0) {
			rt.glutenSeconds--;
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "celiac_slow", -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.glutenSeconds > 0);

		if (!set.contains(Handicap.SEIZURES)) {
			rt.seizureSeconds = 0;
		} else {
			if (rt.seizureSeconds > 0) {
				rt.seizureSeconds--;
			} else {
				boolean hungry = p.getFoodData().getFoodLevel() <= 6 && rnd.nextInt(90) == 0;
				boolean badEffect = (p.hasEffect(MobEffects.POISON) || p.hasEffect(MobEffects.WITHER)) && rnd.nextInt(20) == 0;
				if (hungry || badEffect) {
					startSeizure(p, rt);
				}
			}
		}
		boolean seizing = rt.seizureSeconds > 0;
		setModifier(p, Attributes.MOVEMENT_SPEED, "seizure_move", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, seizing);
		setModifier(p, Attributes.JUMP_STRENGTH, "seizure_jump", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, seizing);

		if (!set.contains(Handicap.OCD)) {
			rt.ocdType = 0;
			rt.ocdCooldown = 30;
		} else if (rt.ocdType == 0) {
			rt.ocdCooldown--;
			if (rt.ocdCooldown <= 0) {
				if (rnd.nextBoolean()) {
					rt.ocdType = 1;
					rt.ocdRemaining = 5;
					p.sendSystemMessage(Component.literal("Compulsion: you must jump 5 times!").withStyle(ChatFormatting.LIGHT_PURPLE));
				} else {
					rt.ocdType = 2;
					rt.ocdRemaining = 6;
					p.sendSystemMessage(Component.literal("Compulsion: you must sneak 6 times!").withStyle(ChatFormatting.LIGHT_PURPLE));
				}
				rt.ocdDeadline = 45;
			}
		} else {
			rt.ocdDeadline--;
			if (rt.ocdDeadline == 15 || rt.ocdDeadline == 5) {
				p.sendSystemMessage(Component.literal("Warning: obey your compulsion! " + rt.ocdRemaining + " left, "
					+ rt.ocdDeadline + " seconds before you get hurt.").withStyle(ChatFormatting.GOLD));
			}
			if (rt.ocdDeadline <= 0) {
				p.sendSystemMessage(Component.literal("You disobeyed your compulsion: -1 heart.").withStyle(ChatFormatting.RED));
				float newHealth = p.getHealth() - 2.0f;
				if (newHealth <= 0.0f) {
					p.kill((ServerLevel) p.level());
				} else {
					p.setHealth(newHealth);
				}
				rt.ocdType = 0;
				rt.ocdCooldown = 60 + rnd.nextInt(60);
			}
		}

		long mask = 0L;
		for (Handicap d : set) {
			mask |= d.bit();
		}
		int flags = (seizing ? 1 : 0) | (rt.narcoSeconds > 0 ? 2 : 0);
		if ((mask != rt.lastMask || flags != rt.lastFlags) && ServerPlayNetworking.canSend(p, SyncPayload.TYPE)) {
			ServerPlayNetworking.send(p, new SyncPayload(mask, flags));
			rt.lastMask = mask;
			rt.lastFlags = flags;
		}
	}

	private static void tickMoreHandicaps(ServerPlayer p, Set<Handicap> set, Runtime rt, RandomSource rnd) {
		setModifier(p, Attributes.ATTACK_DAMAGE, "muscle_weakness", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			set.contains(Handicap.MUSCLE_WEAKNESS));

		if (!set.contains(Handicap.HYPOTENSION)) {
			rt.hypoSeconds = 0;
		} else if (rt.hypoSeconds > 0) {
			rt.hypoSeconds--;
		} else if (rnd.nextInt(60) == 0) {
			rt.hypoSeconds = 8;
			p.sendOverlayMessage(Component.literal("Your blood pressure drops... you feel weak."));
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "hypotension_slow", -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.hypoSeconds > 0);
		setModifier(p, Attributes.ATTACK_DAMAGE, "hypotension_weak", -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.hypoSeconds > 0);

		if (!set.contains(Handicap.POOR_CIRCULATION)) {
			rt.idleSeconds = 0;
			rt.numbSeconds = 0;
		} else {
			double dx = p.getX() - rt.lastX;
			double dz = p.getZ() - rt.lastZ;
			rt.lastX = p.getX();
			rt.lastZ = p.getZ();
			if (dx * dx + dz * dz > 0.04) {
				rt.idleSeconds = 0;
			} else {
				rt.idleSeconds++;
			}
			if (rt.numbSeconds > 0) {
				rt.numbSeconds--;
			} else if (rt.idleSeconds >= 20) {
				rt.numbSeconds = 10;
				rt.idleSeconds = 0;
				p.sendOverlayMessage(Component.literal("Your legs are numb..."));
			}
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "poor_circulation", -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.numbSeconds > 0);

		if (!set.contains(Handicap.CLUMSINESS)) {
			rt.clumsyCooldown = 0;
		} else if (rt.clumsyCooldown > 0) {
			rt.clumsyCooldown--;
		} else if (rnd.nextInt(90) == 0) {
			ItemStack held = p.getMainHandItem();
			if (!held.isEmpty()) {
				ItemStack copy = held.copy();
				p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				p.drop(copy, false);
				p.sendOverlayMessage(Component.literal("Oops! It slipped out of your hand."));
				rt.clumsyCooldown = 60;
			}
		}

		if (!set.contains(Handicap.MONSTER_PHOBIA)) {
			rt.phobiaSeconds = 0;
		} else {
			if (rt.phobiaSeconds > 0) {
				rt.phobiaSeconds--;
			}
			List<Monster> near = p.level().getEntitiesOfClass(Monster.class, p.getBoundingBox().inflate(8.0));
			if (!near.isEmpty()) {
				if (rt.phobiaSeconds == 0) {
					p.sendOverlayMessage(Component.literal("A monster is nearby... you freeze!"));
				}
				rt.phobiaSeconds = 3;
			}
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "monster_phobia_slow", -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.phobiaSeconds > 0);
		setModifier(p, Attributes.ATTACK_DAMAGE, "monster_phobia_weak", -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.phobiaSeconds > 0);

		if (!set.contains(Handicap.CLAUSTROPHOBIA)) {
			rt.claustroSeconds = 0;
		} else {
			if (rt.claustroSeconds > 0) {
				rt.claustroSeconds--;
			}
			if (isEnclosed(p)) {
				if (rt.claustroSeconds == 0) {
					p.sendOverlayMessage(Component.literal("The walls close in on you..."));
				}
				rt.claustroSeconds = 2;
			}
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "claustrophobia", -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.claustroSeconds > 0);

		if (!set.contains(Handicap.SLOW_REFLEXES)) {
			rt.reflexSeconds = 0;
		} else if (rt.reflexSeconds > 0) {
			rt.reflexSeconds--;
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "slow_reflexes", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.reflexSeconds > 0);
	}

	private static boolean isEnclosed(ServerPlayer p) {
		BlockPos base = p.blockPosition();
		int solid = 0;
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, 0, -2), base.offset(2, 2, 2))) {
			if (!p.level().getBlockState(pos).getCollisionShape(p.level(), pos).isEmpty()) {
				solid++;
			}
		}
		return solid >= 52;
	}

	private static void startSeizure(ServerPlayer p, Runtime rt) {
		rt.seizureSeconds = 6;
		p.sendOverlayMessage(Component.literal("Seizure!"));
	}

	private static void tickFast(ServerPlayer p) {
		UUID id = p.getUUID();
		Runtime rt = RUNTIME.computeIfAbsent(id, k -> new Runtime());

		boolean lactose = state.has(id, Handicap.LACTOSE_INTOLERANCE);
		boolean diabetes = state.has(id, Handicap.DIABETES);
		boolean celiac = state.has(id, Handicap.CELIAC);
		boolean fishAllergy = state.has(id, Handicap.FISH_ALLERGY);
		if (lactose || diabetes || celiac || fishAllergy) {
			if (p.isUsingItem()) {
				rt.useItem = p.getUseItem().copy();
				rt.useRemaining = p.getUseItemRemainingTicks();
			} else if (rt.useItem != null) {
				if (rt.useRemaining <= 2) {
					onConsumed(p, rt.useItem, lactose, diabetes, celiac, fishAllergy);
				}
				rt.useItem = null;
			}
		} else {
			rt.useItem = null;
		}

		float health = p.getHealth();
		if (state.has(id, Handicap.SEIZURES)) {
			if (rt.lastHealth >= 0f && health < rt.lastHealth - 0.5f && rt.seizureSeconds == 0 && p.getRandom().nextInt(5) == 0) {
				startSeizure(p, rt);
			}
		}
		if (state.has(id, Handicap.SLOW_REFLEXES) && rt.lastHealth >= 0f && health < rt.lastHealth - 0.01f) {
			rt.reflexSeconds = 4;
		}
		rt.lastHealth = health;

		if (state.has(id, Handicap.RESPIRATORY_FAILURE) && p.isUnderWater() && !p.hasEffect(MobEffects.WATER_BREATHING)) {
			p.setAirSupply(p.getAirSupply() - 1);
		}

		boolean onGround = p.onGround();
		boolean sneaking = p.isShiftKeyDown();
		if (state.has(id, Handicap.OCD) && rt.ocdType != 0) {
			if (rt.ocdType == 1 && rt.lastOnGround && !onGround && p.getDeltaMovement().y > 0.2) {
				rt.ocdRemaining--;
			} else if (rt.ocdType == 2 && sneaking && !rt.lastSneak) {
				rt.ocdRemaining--;
			}
			if (rt.ocdRemaining <= 0) {
				rt.ocdType = 0;
				rt.ocdCooldown = 60 + p.getRandom().nextInt(120);
				p.sendSystemMessage(Component.literal("Compulsion fulfilled.").withStyle(ChatFormatting.GREEN));
			}
		}
		rt.lastOnGround = onGround;
		rt.lastSneak = sneaking;
	}

	private static void onConsumed(ServerPlayer p, ItemStack item, boolean lactose, boolean diabetes, boolean celiac,
			boolean fishAllergy) {
		if (fishAllergy && (item.is(Items.COD) || item.is(Items.SALMON) || item.is(Items.COOKED_COD)
			|| item.is(Items.COOKED_SALMON) || item.is(Items.TROPICAL_FISH))) {
			p.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
			p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 300, 1));
			p.sendOverlayMessage(Component.literal("Fish! Your allergy kicks in..."));
		}
		if (celiac && (item.is(Items.BREAD) || item.is(Items.COOKIE))) {
			RUNTIME.computeIfAbsent(p.getUUID(), k -> new Runtime()).glutenSeconds = 30;
			p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 1));
			p.sendOverlayMessage(Component.literal("Gluten! You feel sick..."));
		}
		if (lactose && item.is(Items.MILK_BUCKET)) {
			p.sendSystemMessage(Component.literal("Your lactose intolerance was fatal...").withStyle(ChatFormatting.RED));
			p.kill((ServerLevel) p.level());
			return;
		}
		if (diabetes) {
			int damage = sugarDamage(item);
			if (damage > 0) {
				p.setHealth(Math.max(1.0f, p.getHealth() - damage));
				p.sendOverlayMessage(Component.literal("Too much sugar..."));
			}
		}
	}

	private static int sugarDamage(ItemStack item) {
		if (item.is(Items.HONEY_BOTTLE) || item.is(Items.PUMPKIN_PIE)) {
			return 4;
		}
		if (item.is(Items.COOKIE) || item.is(Items.SWEET_BERRIES) || item.is(Items.GLOW_BERRIES) || item.is(Items.MELON_SLICE)) {
			return 1;
		}
		return 0;
	}

	private static void setModifier(ServerPlayer p, Holder<Attribute> attribute, String name, double amount,
			AttributeModifier.Operation operation, boolean active) {
		AttributeInstance inst = p.getAttribute(attribute);
		if (inst == null) {
			return;
		}
		Identifier id = HandicapMod.id(name);
		AttributeModifier existing = inst.getModifier(id);
		if (active) {
			if (existing == null || existing.amount() != amount) {
				inst.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
			}
		} else if (existing != null) {
			inst.removeModifier(id);
		}
	}
}
