package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.HandicapMod;

import java.lang.reflect.Method;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class ClientEffects {
	private static double savedVolume = -1.0;
	private static int vertigoTicks = 0;
	private static int wobbleTick = 0;
	private static Boolean savedSubtitles = null;
	private static Identifier currentEffect = null;
	private static int effectTicks = 0;
	private static int invertedTicks = 0;
	private static int invertedCooldown = 400;
	private static int migraineTicks = 0;
	private static int migraineCooldown = 600;
	private static int tunnelTicks = 0;
	private static int flashTicks = 0;
	private static boolean controlsSwapped = false;
	private static int jumpCurseCooldown = 200;
	private static int sleepwalkTicks = 0;
	private static int sneezeSlowTicks = 0;
	private static int magnetTicks = 0;
	private static boolean magnetLastGround = true;
	private static int stickyLock = 0;
	private static int lastSlot = -1;
	private static boolean postEffectWarned = false;

	private static final Identifier COLOR_BLIND_EFFECT = HandicapMod.id("colorblind");
	private static final Identifier FLIP_EFFECT = HandicapMod.id("flip");
	private static final Identifier MYOPIA_EFFECT = HandicapMod.id("myopia");
	private static final Identifier HYPEROPIA_EFFECT = HandicapMod.id("hyperopia");
	private static final Identifier TUNNEL_EFFECT = HandicapMod.id("tunnel");
	private static final Identifier AGNOSIA_EFFECT = HandicapMod.id("agnosia");
	private static final Identifier MIGRAINE_EFFECT = HandicapMod.id("migraine");
	private static final Identifier ICE_ID = HandicapMod.id("ice_rink_client");
	private static final Identifier BLIND_EFFECT = HandicapMod.id("blind");
	private static final Identifier GLOW_1_EFFECT = HandicapMod.id("glow1");
	private static final Identifier GLOW_2_EFFECT = HandicapMod.id("glow2");
	private static final Identifier GLOW_3_EFFECT = HandicapMod.id("glow3");
	private static final Identifier SNEEZE_ID = HandicapMod.id("sneezing_client");
	private static final Identifier MAGNET_ID = HandicapMod.id("magnet_feet_client");

	private static final Identifier LEG_ID = HandicapMod.id("amputated_leg_client");

	private static final String[] FAKE_NAMES = {"Herobrine", "Steve_", "xX_Shadow_Xx", "Entity303"};

	private ClientEffects() {
	}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null) {
			restoreSound(mc);
			vertigoTicks = 0;
			currentEffect = null;
			effectTicks = 0;
			invertedTicks = 0;
			tunnelTicks = 0;
			flashTicks = 0;
			migraineTicks = 0;
			lastSlot = -1;
			if (controlsSwapped) {
				swapControls(mc);
			}
			return;
		}
		RandomSource rnd = p.getRandom();

		boolean deaf = ClientState.has(Handicap.DEAF);
		boolean partiallyDeaf = ClientState.has(Handicap.PARTIAL_DEAFNESS);
		if (deaf || partiallyDeaf) {
			if (savedVolume < 0) {
				savedVolume = mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
			}
			double target = deaf ? 0.0 : savedVolume * 0.5;
			if (Math.abs(mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).get() - target) > 0.001) {
				mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(target);
			}
			if (deaf && savedSubtitles == null) {
				savedSubtitles = mc.options.showSubtitles().get();
				mc.options.showSubtitles().set(true);
			}
		} else {
			restoreSound(mc);
		}

		if (ClientState.has(Handicap.KLEPTOMANIA)) {
			boolean tried = false;
			while (mc.options.keyDrop.consumeClick()) {
				tried = true;
			}
			if (tried) {
				p.sendOverlayMessage(Component.literal("You can't let go of this item!"));
			}
		}

		if (ClientState.has(Handicap.INVERTED_VISION)) {
			if (invertedTicks > 0) {
				invertedTicks--;
			} else if (invertedCooldown > 0) {
				invertedCooldown--;
			} else {
				invertedTicks = 300;
				invertedCooldown = 400 + rnd.nextInt(400);
				p.sendOverlayMessage(Component.literal("Your world flips upside down!"));
			}
		} else {
			invertedTicks = 0;
		}

		if (ClientState.has(Handicap.TUNNEL_VISION)) {
			if (tunnelTicks > 0) {
				tunnelTicks--;
			} else if (rnd.nextInt(1200) == 0) {
				tunnelTicks = 600;
				p.sendOverlayMessage(Component.literal("Your field of view narrows..."));
			}
		} else {
			tunnelTicks = 0;
		}

		if (ClientState.has(Handicap.PHANTOM_LIGHT)) {
			if (flashTicks > 0) {
				flashTicks--;
			} else if (rnd.nextInt(900) == 0) {
				flashTicks = 90;
			}
		} else {
			flashTicks = 0;
		}

		boolean shouldSwap = ClientState.has(Handicap.SPATIAL_DISORIENTATION);
		if (shouldSwap != controlsSwapped) {
			swapControls(mc);
		}

		if (ClientState.has(Handicap.MIGRAINE)) {
			if (migraineTicks > 0) {
				migraineTicks--;
			} else if (migraineCooldown > 0) {
				migraineCooldown--;
			} else {
				migraineTicks = 400;
				migraineCooldown = 800 + rnd.nextInt(800);
				p.sendOverlayMessage(Component.literal("A migraine is starting..."));
			}
		} else {
			migraineTicks = 0;
		}

		Identifier desired = null;
		if (ClientState.has(Handicap.BLIND)) {
			desired = BLIND_EFFECT;
		} else if (flashTicks > 0) {
			desired = glowEffect(flashTicks);
		} else if (invertedTicks > 0) {
			desired = FLIP_EFFECT;
		} else if (tunnelTicks > 0) {
			desired = TUNNEL_EFFECT;
		} else if (migraineTicks > 0) {
			desired = MIGRAINE_EFFECT;
		} else if (ClientState.has(Handicap.COLOR_BLIND)) {
			desired = COLOR_BLIND_EFFECT;
		} else if (ClientState.has(Handicap.VISUAL_AGNOSIA)) {
			desired = AGNOSIA_EFFECT;
		} else if (ClientState.has(Handicap.MYOPIA)) {
			desired = MYOPIA_EFFECT;
		} else if (ClientState.has(Handicap.HYPEROPIA)) {
			desired = HYPEROPIA_EFFECT;
		}
		if (desired != null) {
			effectTicks++;
			if (!desired.equals(currentEffect) || effectTicks % 100 == 0) {
				setPostEffect(mc, desired);
				currentEffect = desired;
			}
		} else if (currentEffect != null) {
			clearPostEffect(mc);
			currentEffect = null;
			effectTicks = 0;
		}

		if (ClientState.has(Handicap.PARAPLEGIA)) {
			p.setSprinting(false);
		}

		if (ClientState.has(Handicap.TINNITUS) && rnd.nextInt(900) == 0) {
			for (int i = 0; i < 3; i++) {
				mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.7f, 1.8f + 0.1f * i, false);
			}
		}

		if (ClientState.has(Handicap.HICCUPS) && rnd.nextInt(500) == 0) {
			p.setXRot(Mth.clamp(p.getXRot() - 6.0f, -90.0f, 90.0f));
			mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_BURP,
				SoundSource.PLAYERS, 0.5f, 1.8f, false);
		}

		if (ClientState.has(Handicap.SNEEZING) && rnd.nextInt(900) == 0) {
			p.setXRot(Mth.clamp(p.getXRot() + 14.0f, -90.0f, 90.0f));
			sneezeSlowTicks = 10;
			mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(), SoundEvents.PANDA_SNEEZE,
				SoundSource.PLAYERS, 0.8f, 1.0f, false);
		}
		if (sneezeSlowTicks > 0) {
			sneezeSlowTicks--;
		}
		setLocalModifier(p, Attributes.MOVEMENT_SPEED, SNEEZE_ID, -0.8, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			ClientState.has(Handicap.SNEEZING) && sneezeSlowTicks > 0);

		if (ClientState.has(Handicap.JUMP_CURSE)) {
			if (jumpCurseCooldown > 0) {
				jumpCurseCooldown--;
			} else if (p.onGround()) {
				Vec3 v = p.getDeltaMovement();
				p.setDeltaMovement(v.x, 0.42, v.z);
				jumpCurseCooldown = 300 + rnd.nextInt(600);
			}
		}

		boolean groundNow = p.onGround();
		if (groundNow && !magnetLastGround) {
			magnetTicks = 30;
		}
		magnetLastGround = groundNow;
		if (magnetTicks > 0) {
			magnetTicks--;
		}
		setLocalModifier(p, Attributes.JUMP_STRENGTH, MAGNET_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
			ClientState.has(Handicap.MAGNET_FEET) && magnetTicks > 0);

		if (ClientState.has(Handicap.SLEEPWALKING)) {
			if (sleepwalkTicks > 0) {
				sleepwalkTicks--;
				Vec3 look = p.getLookAngle();
				double len = Math.sqrt(look.x * look.x + look.z * look.z);
				if (len > 0.001) {
					Vec3 v = p.getDeltaMovement();
					p.setDeltaMovement(look.x / len * 0.15, v.y, look.z / len * 0.15);
				}
			} else if (rnd.nextInt(1800) == 0) {
				sleepwalkTicks = 60;
				p.sendOverlayMessage(Component.literal("You are sleepwalking..."));
			}
		} else {
			sleepwalkTicks = 0;
		}

		int slot = p.getInventory().getSelectedSlot();
		if (lastSlot < 0) {
			lastSlot = slot;
		}
		if (ClientState.has(Handicap.STICKY_HAND)) {
			if (stickyLock > 0) {
				stickyLock--;
				if (slot != lastSlot) {
					p.getInventory().setSelectedSlot(lastSlot);
				}
			} else if (slot != lastSlot) {
				lastSlot = slot;
				stickyLock = 60;
			}
		} else {
			lastSlot = slot;
			stickyLock = 0;
		}

		boolean iceSlide = false;
		if (ClientState.has(Handicap.ICE_RINK) && p.onGround()) {
			float friction = p.getBlockStateOn().getBlock().getFriction();
			if (friction < 0.9f) {
				Vec3 v = p.getDeltaMovement();
				float k = 0.98f / friction;
				p.setDeltaMovement(v.x * k, v.y, v.z * k);
				iceSlide = true;
			}
		}
		setLocalModifier(p, Attributes.MOVEMENT_SPEED, ICE_ID, -0.77, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, iceSlide);

		if (ClientState.has(Handicap.HAND_TREMORS) && mc.screen == null && mc.options.keyAttack.isDown() && rnd.nextInt(80) == 0) {
			mc.options.keyAttack.setDown(false);
		}

		setLegModifier(p, ClientState.has(Handicap.AMPUTATED_LEG) && p.onGround());

		if (ClientState.has(Handicap.PARKINSONS)) {
			p.setYRot(p.getYRot() + (rnd.nextFloat() - 0.5f) * 1.2f);
			p.setXRot(Mth.clamp(p.getXRot() + (rnd.nextFloat() - 0.5f) * 1.2f, -90.0f, 90.0f));
		}

		if (ClientState.has(Handicap.TOURETTES) && rnd.nextInt(200) == 0) {
			float sign = rnd.nextBoolean() ? 1.0f : -1.0f;
			p.setYRot(p.getYRot() + sign * (30.0f + rnd.nextFloat() * 50.0f));
			p.setXRot(Mth.clamp(p.getXRot() + (rnd.nextFloat() - 0.5f) * 40.0f, -90.0f, 90.0f));
		}

		if (ClientState.has(Handicap.CEREBRAL_PALSY) && rnd.nextInt(100) == 0) {
			Vec3 v = p.getDeltaMovement();
			p.setDeltaMovement(v.x + (rnd.nextDouble() - 0.5) * 0.8, v.y, v.z + (rnd.nextDouble() - 0.5) * 0.8);
		}

		if (ClientState.has(Handicap.ADHD) && rnd.nextInt(160) == 0) {
			p.getInventory().setSelectedSlot(rnd.nextInt(9));
		}

		if (ClientState.has(Handicap.VERTIGO)) {
			if (vertigoTicks > 0) {
				vertigoTicks--;
				wobbleTick++;
				p.setYRot(p.getYRot() + 5.0f);
				p.setXRot(Mth.clamp(p.getXRot() + Mth.sin(wobbleTick * 0.2f) * 1.5f, -90.0f, 90.0f));
			} else if (rnd.nextInt(1200) == 0) {
				vertigoTicks = 100;
				p.sendOverlayMessage(Component.literal("Everything is spinning around you..."));
			}
		} else {
			vertigoTicks = 0;
		}

		if (ClientState.asleep()) {
			p.setXRot(p.getXRot() + (80.0f - p.getXRot()) * 0.15f);
		}

		if (ClientState.seizing()) {
			p.setYRot(p.getYRot() + (rnd.nextFloat() - 0.5f) * 30.0f);
			p.setXRot(Mth.clamp(p.getXRot() + (rnd.nextFloat() - 0.5f) * 20.0f, -90.0f, 90.0f));
		}

		if (ClientState.has(Handicap.SCHIZOPHRENIA) && rnd.nextInt(600) == 0) {
			if (rnd.nextBoolean()) {
				SoundEvent[] sounds = {
					SoundEvents.CREEPER_PRIMED, SoundEvents.ZOMBIE_AMBIENT, SoundEvents.SKELETON_AMBIENT,
					SoundEvents.ENDERMAN_STARE, SoundEvents.SPIDER_AMBIENT
				};
				SoundEvent sound = sounds[rnd.nextInt(sounds.length)];
				double dx = (rnd.nextDouble() - 0.5) * 12.0;
				double dz = (rnd.nextDouble() - 0.5) * 12.0;
				mc.level.playLocalSound(p.getX() + dx, p.getY(), p.getZ() + dz, sound, SoundSource.HOSTILE, 1.0f, 1.0f, false);
			} else {
				String name = FAKE_NAMES[rnd.nextInt(FAKE_NAMES.length)];
				Component msg = rnd.nextBoolean()
					? Component.translatable("multiplayer.player.joined", name).withStyle(ChatFormatting.YELLOW)
					: Component.literal("<" + name + "> I can see you");
				p.sendSystemMessage(msg);
			}
		}
	}

	private static void restoreSound(Minecraft mc) {
		if (savedSubtitles != null) {
			mc.options.showSubtitles().set(savedSubtitles);
			savedSubtitles = null;
		}
		if (savedVolume >= 0) {
			mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(savedVolume);
			savedVolume = -1.0;
		}
	}

	public static void onShutdown(Minecraft mc) {
		restoreSound(mc);
		if (currentEffect != null) {
			clearPostEffect(mc);
			currentEffect = null;
			effectTicks = 0;
		}
		invertedTicks = 0;
		tunnelTicks = 0;
		flashTicks = 0;
		migraineTicks = 0;
		lastSlot = -1;
		if (controlsSwapped) {
			swapControls(mc);
		}
		ClientState.reset();
		vertigoTicks = 0;
	}

	private static void setPostEffect(Minecraft mc, Identifier id) {
		try {
			Method m = mc.gameRenderer.getClass().getDeclaredMethod("setPostEffect", Identifier.class);
			m.setAccessible(true);
			m.invoke(mc.gameRenderer, id);
		} catch (Throwable t) {
			warnPostEffect(t);
		}
	}

	private static void clearPostEffect(Minecraft mc) {
		try {
			Method m = mc.gameRenderer.getClass().getDeclaredMethod("clearPostEffect");
			m.setAccessible(true);
			m.invoke(mc.gameRenderer);
		} catch (Throwable t) {
			warnPostEffect(t);
		}
	}

	private static void warnPostEffect(Throwable t) {
		if (!postEffectWarned) {
			postEffectWarned = true;
			HandicapMod.LOGGER.warn("Color Blind: could not change the post effect", t);
		}
	}

	private static Identifier glowEffect(int ticksLeft) {
		int t = 90 - ticksLeft;
		if (t < 15 || t >= 75) {
			return GLOW_1_EFFECT;
		}
		if (t < 30 || t >= 60) {
			return GLOW_2_EFFECT;
		}
		return GLOW_3_EFFECT;
	}

	private static void swapControls(Minecraft mc) {
		try {
			KeyMapping left = mc.options.keyLeft;
			KeyMapping right = mc.options.keyRight;
			InputConstants.Key leftKey = InputConstants.getKey(left.saveString());
			InputConstants.Key rightKey = InputConstants.getKey(right.saveString());
			left.setKey(rightKey);
			right.setKey(leftKey);
			KeyMapping.resetMapping();
			controlsSwapped = !controlsSwapped;
		} catch (Throwable t) {
			HandicapMod.LOGGER.warn("Spatial Disorientation: could not swap the movement keys", t);
		}
	}

	private static void setLocalModifier(LocalPlayer p, Holder<Attribute> attribute, Identifier id, double amount,
			AttributeModifier.Operation operation, boolean active) {
		AttributeInstance inst = p.getAttribute(attribute);
		if (inst == null) {
			return;
		}
		boolean present = inst.getModifier(id) != null;
		if (active && !present) {
			inst.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
		} else if (!active && present) {
			inst.removeModifier(id);
		}
	}

	private static void setLegModifier(LocalPlayer p, boolean active) {
		AttributeInstance inst = p.getAttribute(Attributes.MOVEMENT_SPEED);
		if (inst == null) {
			return;
		}
		boolean present = inst.getModifier(LEG_ID) != null;
		if (active && !present) {
			inst.addOrReplacePermanentModifier(
				new AttributeModifier(LEG_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else if (!active && present) {
			inst.removeModifier(LEG_ID);
		}
	}
}
