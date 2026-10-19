package com.handicap.client;

import com.handicap.Handicap;
import com.handicap.HandicapMod;

import java.lang.reflect.Method;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class ClientEffects {
	private static double savedVolume = -1.0;
	private static int vertigoTicks = 0;
	private static int wobbleTick = 0;
	private static boolean colorBlindOn = false;
	private static int colorBlindTicks = 0;
	private static boolean postEffectWarned = false;

	private static final Identifier COLOR_BLIND_EFFECT = HandicapMod.id("colorblind");

	private static final Identifier LEG_ID = HandicapMod.id("amputated_leg_client");

	private static final String[] FAKE_NAMES = {"Herobrine", "Steve_", "xX_Shadow_Xx", "Entity303"};

	private ClientEffects() {
	}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null) {
			restoreSound(mc);
			vertigoTicks = 0;
			colorBlindOn = false;
			colorBlindTicks = 0;
			return;
		}
		RandomSource rnd = p.getRandom();

		if (ClientState.has(Handicap.DEAF)) {
			if (savedVolume < 0) {
				savedVolume = mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
				mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(0.0);
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

		if (ClientState.has(Handicap.COLOR_BLIND)) {
			colorBlindTicks++;
			if (colorBlindTicks % 100 == 1) {
				setPostEffect(mc, COLOR_BLIND_EFFECT);
			}
			colorBlindOn = true;
		} else if (colorBlindOn) {
			clearPostEffect(mc);
			colorBlindOn = false;
			colorBlindTicks = 0;
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
		if (savedVolume >= 0) {
			mc.options.getSoundSourceOptionInstance(SoundSource.MASTER).set(savedVolume);
			savedVolume = -1.0;
		}
	}

	public static void onShutdown(Minecraft mc) {
		restoreSound(mc);
		if (colorBlindOn) {
			clearPostEffect(mc);
			colorBlindOn = false;
			colorBlindTicks = 0;
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
