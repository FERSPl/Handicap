package com.handicap.client.mixin;

import com.handicap.client.Prosopagnosia;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
	@Shadow
	@Final
	private GameProfile profile;

	@Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
	private void handicap$steve(CallbackInfoReturnable<PlayerSkin> cir) {
		if (Prosopagnosia.hides(profile.id())) {
			cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin());
		}
	}
}
