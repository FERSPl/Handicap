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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HandicapManager {
	private static HandicapState state;
	private static MinecraftServer server;
	private static final Map<UUID, Runtime> RUNTIME = new HashMap<>();

	private static final String[] TOURETTE_WORDS = {
		"BIP !", "OUI OUI OUI", "PATATE !", "AAAAH", "non non non", "BOUM", "HÉ !", "*couinement*"
	};

	/** Compteurs temporaires par joueur (non sauvegardés). */
	private static final class Runtime {
		int cancerSeconds;
		int sprintSeconds;
		int windedSeconds;
		int narcoSeconds;
		boolean blindApplied;
		// consommation d'objets
		ItemStack useItem;
		int useRemaining;
		// épilepsie
		float lastHealth = -1f;
		int seizureSeconds;
		// TOC (0 = aucune, 1 = sauter, 2 = s'accroupir)
		int ocdType;
		int ocdRemaining;
		int ocdDeadline;
		int ocdCooldown = 30;
		boolean lastOnGround = true;
		boolean lastSneak;
		// synchronisation client
		int lastMask = -1;
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
		// Quand un joueur (re)joint, on renvoie l'état au client
		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> RUNTIME.remove(handler.getPlayer().getUUID()));

		// Muet : bloque les messages du chat
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			if (state != null && state.has(sender.getUUID(), Handicap.MUTE)) {
				sender.sendOverlayMessage(Component.literal("Tu es muet : impossible de parler."));
				return false;
			}
			return true;
		});
	}

	// ------------------------------------------------------------------
	// Donner / retirer
	// ------------------------------------------------------------------

	public static boolean give(ServerPlayer p, Handicap d, boolean announce) {
		if (!state.give(p.getUUID(), d)) {
			return false;
		}
		if (announce && !state.has(p.getUUID(), Handicap.ANOSOGNOSIA)) {
			p.sendSystemMessage(Component.literal("Nouveau handicap : " + d.displayName + " — " + d.description)
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
			p.sendSystemMessage(Component.literal("Handicap retiré : " + d.displayName)
				.withStyle(ChatFormatting.GREEN));
		}
		refresh(p);
		return true;
	}

	/** Donne un handicap aléatoire que le joueur n'a pas encore. Renvoie null s'il les a tous. */
	public static Handicap giveRandom(ServerPlayer p) {
		List<Handicap> available = new ArrayList<>();
		for (Handicap d : Handicap.values()) {
			if (!state.has(p.getUUID(), d)) {
				available.add(d);
			}
		}
		if (available.isEmpty()) {
			p.sendSystemMessage(Component.literal("Tu as déjà tous les handicaps !").withStyle(ChatFormatting.GRAY));
			return null;
		}
		Handicap d = available.get(p.getRandom().nextInt(available.size()));
		give(p, d, true);
		return d;
	}

	/** Retire un handicap aléatoire parmi ceux du joueur. Renvoie null s'il n'en a aucun. */
	public static Handicap removeRandom(ServerPlayer p) {
		List<Handicap> owned = new ArrayList<>(state.get(p.getUUID()));
		if (owned.isEmpty()) {
			return null;
		}
		Handicap d = owned.get(p.getRandom().nextInt(owned.size()));
		remove(p, d, true);
		return d;
	}

	/** Réapplique immédiatement les effets (après une commande, par exemple). */
	public static void refresh(ServerPlayer p) {
		if (state != null && server != null) {
			tickPlayer(p);
		}
	}

	// ------------------------------------------------------------------
	// Boucle principale
	// ------------------------------------------------------------------

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

	/** Appelé une fois par seconde pour chaque joueur. */
	private static void tickPlayer(ServerPlayer p) {
		UUID id = p.getUUID();
		Set<Handicap> set = state.get(id);
		Runtime rt = RUNTIME.computeIfAbsent(id, k -> new Runtime());
		RandomSource rnd = p.getRandom();

		// --- Aveugle ---
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

		// --- Petite taille ---
		boolean isShort = set.contains(Handicap.SHORT);
		setModifier(p, Attributes.SCALE, "short_scale", -0.28, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, isShort);
		setModifier(p, Attributes.BLOCK_INTERACTION_RANGE, "short_block_reach", -1.0, AttributeModifier.Operation.ADD_VALUE, isShort);
		setModifier(p, Attributes.ENTITY_INTERACTION_RANGE, "short_entity_reach", -0.75, AttributeModifier.Operation.ADD_VALUE, isShort);

		// --- Arthrite ---
		boolean arthritis = set.contains(Handicap.ARTHRITIS);
		setModifier(p, Attributes.BLOCK_BREAK_SPEED, "arthritis_mining", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, arthritis);
		setModifier(p, Attributes.ATTACK_SPEED, "arthritis_attack", -0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, arthritis);

		// --- Os fragiles ---
		setModifier(p, Attributes.FALL_DAMAGE_MULTIPLIER, "brittle_bones", 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			set.contains(Handicap.BRITTLE_BONES));

		// --- Cancer : -1 cœur par minute, il reste au minimum 1 cœur ---
		int stage = state.cancerStage(id);
		if (set.contains(Handicap.CANCER)) {
			rt.cancerSeconds++;
			if (rt.cancerSeconds >= 60) {
				rt.cancerSeconds = 0;
				if (stage < 9) {
					stage++;
					state.setCancerStage(id, stage);
					p.sendOverlayMessage(Component.literal("Ta santé se dégrade..."));
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

		// --- Asthme : après 10 s de sprint, essoufflé pendant 8 s ---
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
				p.sendOverlayMessage(Component.literal("Tu manques d'air !"));
			}
		} else {
			rt.sprintSeconds = Math.max(0, rt.sprintSeconds - 2);
		}
		setModifier(p, Attributes.MOVEMENT_SPEED, "asthma_winded", -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			rt.windedSeconds > 0);

		// --- Narcolepsie : s'endort 5 s au hasard (environ toutes les 2 min) ---
		if (!set.contains(Handicap.NARCOLEPSY)) {
			rt.narcoSeconds = 0;
		} else if (rt.narcoSeconds > 0) {
			rt.narcoSeconds--;
		} else if (rnd.nextInt(120) == 0) {
			rt.narcoSeconds = 5;
			p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0, false, false, false));
			p.sendOverlayMessage(Component.literal("Zzz..."));
		}
		boolean asleep = rt.narcoSeconds > 0;
		setModifier(p, Attributes.MOVEMENT_SPEED, "narcolepsy_move", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, asleep);
		setModifier(p, Attributes.JUMP_STRENGTH, "narcolepsy_jump", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, asleep);

		// --- Insomnie ---
		if (set.contains(Handicap.INSOMNIA) && p.isSleeping()) {
			p.stopSleepInBed(true, true);
			p.sendOverlayMessage(Component.literal("Tu n'arrives pas à dormir."));
		}

		// --- Main secondaire amputée ---
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

		// --- Démence : échange deux emplacements de temps en temps ---
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

		// --- Allergie au pollen : fleurs à moins de 2 blocs ---
		if (set.contains(Handicap.POLLEN_ALLERGY) && !p.hasEffect(MobEffects.POISON)) {
			BlockPos base = p.blockPosition();
			for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(2, 2, 2))) {
				if (p.level().getBlockState(pos).is(BlockTags.FLOWERS)) {
					p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
					p.sendOverlayMessage(Component.literal("Atchoum ! Le pollen t'empoisonne."));
					break;
				}
			}
		}

		// --- Tourette : dit un mot au hasard dans le chat (environ toutes les 90 s) ---
		if (set.contains(Handicap.TOURETTES) && rnd.nextInt(90) == 0) {
			String word = TOURETTE_WORDS[rnd.nextInt(TOURETTE_WORDS.length)];
			Component msg = Component.translatable("chat.type.text", p.getDisplayName(), Component.literal(word));
			server.getPlayerList().broadcastSystemMessage(msg, false);
		}

		tickPlayerExtra(p, set, rt);
	}

	// ------------------------------------------------------------------
	// Partie « une fois par seconde » supplémentaire (épilepsie, TOC, synchro)
	// ------------------------------------------------------------------

	private static void tickPlayerExtra(ServerPlayer p, Set<Handicap> set, Runtime rt) {
		RandomSource rnd = p.getRandom();

		// --- Épilepsie : faim basse ou effet négatif => risque de crise ---
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

		// --- TOC : une compulsion à la fois ---
		if (!set.contains(Handicap.OCD)) {
			rt.ocdType = 0;
			rt.ocdCooldown = 30;
		} else if (rt.ocdType == 0) {
			rt.ocdCooldown--;
			if (rt.ocdCooldown <= 0) {
				if (rnd.nextBoolean()) {
					rt.ocdType = 1;
					rt.ocdRemaining = 5;
					p.sendSystemMessage(Component.literal("Compulsion : tu dois sauter 5 fois !").withStyle(ChatFormatting.LIGHT_PURPLE));
				} else {
					rt.ocdType = 2;
					rt.ocdRemaining = 6;
					p.sendSystemMessage(Component.literal("Compulsion : tu dois t'accroupir 6 fois !").withStyle(ChatFormatting.LIGHT_PURPLE));
				}
				rt.ocdDeadline = 45;
			}
		} else {
			rt.ocdDeadline--;
			if (rt.ocdDeadline <= 0) {
				p.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
				p.sendSystemMessage(Component.literal("Tu n'as pas pu t'en empêcher... l'angoisse te rend malade.").withStyle(ChatFormatting.RED));
				rt.ocdType = 0;
				rt.ocdCooldown = 60 + rnd.nextInt(60);
			}
		}

		// --- Synchronisation avec le client (effets visuels et sonores) ---
		int mask = 0;
		for (Handicap d : set) {
			mask |= d.bit();
		}
		int flags = seizing ? 1 : 0;
		if ((mask != rt.lastMask || flags != rt.lastFlags) && ServerPlayNetworking.canSend(p, SyncPayload.TYPE)) {
			ServerPlayNetworking.send(p, new SyncPayload(mask, flags));
			rt.lastMask = mask;
			rt.lastFlags = flags;
		}
	}

	private static void startSeizure(ServerPlayer p, Runtime rt) {
		rt.seizureSeconds = 6;
		p.sendOverlayMessage(Component.literal("Crise d'épilepsie !"));
	}

	// ------------------------------------------------------------------
	// Partie « à chaque tick » (consommation, dégâts, compulsions)
	// ------------------------------------------------------------------

	private static void tickFast(ServerPlayer p) {
		UUID id = p.getUUID();
		Runtime rt = RUNTIME.computeIfAbsent(id, k -> new Runtime());

		// --- Détection d'un aliment / d'une boisson consommé (lait, sucre) ---
		boolean lactose = state.has(id, Handicap.LACTOSE_INTOLERANCE);
		boolean diabetes = state.has(id, Handicap.DIABETES);
		if (lactose || diabetes) {
			if (p.isUsingItem()) {
				rt.useItem = p.getUseItem().copy();
				rt.useRemaining = p.getUseItemRemainingTicks();
			} else if (rt.useItem != null) {
				if (rt.useRemaining <= 2) {
					onConsumed(p, rt.useItem, lactose, diabetes);
				}
				rt.useItem = null;
			}
		} else {
			rt.useItem = null;
		}

		// --- Épilepsie : une perte de vie peut déclencher une crise ---
		float health = p.getHealth();
		if (state.has(id, Handicap.SEIZURES)) {
			if (rt.lastHealth >= 0f && health < rt.lastHealth - 0.5f && rt.seizureSeconds == 0 && p.getRandom().nextInt(5) == 0) {
				startSeizure(p, rt);
			}
		}
		rt.lastHealth = health;

		// --- TOC : suivi des sauts et des accroupissements ---
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
				p.sendSystemMessage(Component.literal("Compulsion satisfaite.").withStyle(ChatFormatting.GREEN));
			}
		}
		rt.lastOnGround = onGround;
		rt.lastSneak = sneaking;
	}

	private static void onConsumed(ServerPlayer p, ItemStack item, boolean lactose, boolean diabetes) {
		if (lactose && item.is(Items.MILK_BUCKET)) {
			p.sendSystemMessage(Component.literal("Ton intolérance au lactose t'a été fatale...").withStyle(ChatFormatting.RED));
			p.kill((ServerLevel) p.level());
			return;
		}
		if (diabetes) {
			int damage = sugarDamage(item);
			if (damage > 0) {
				p.setHealth(Math.max(1.0f, p.getHealth() - damage));
				p.sendOverlayMessage(Component.literal("Trop de sucre..."));
			}
		}
	}

	/** Dégâts (en demi-cœurs) infligés par un aliment sucré pour un diabétique. */
	private static int sugarDamage(ItemStack item) {
		if (item.is(Items.HONEY_BOTTLE) || item.is(Items.PUMPKIN_PIE)) {
			return 4;
		}
		if (item.is(Items.COOKIE) || item.is(Items.SWEET_BERRIES) || item.is(Items.GLOW_BERRIES) || item.is(Items.MELON_SLICE)) {
			return 1;
		}
		return 0;
	}

	/** Ajoute ou retire un modificateur d'attribut permanent selon {@code active}. */
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
