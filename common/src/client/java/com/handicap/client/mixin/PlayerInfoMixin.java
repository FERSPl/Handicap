package com.handicap.client.mixin;

import com.handicap.client.Prosopagnosia;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

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

	@Inject(method = "getSkinLocation", at = @At("HEAD"), cancellable = true)
	private void handicap$steve(CallbackInfoReturnable<ResourceLocation> cir) {
		if (Prosopagnosia.hides(profile.getId())) {
			cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin());
		}
	}

	@Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
	private void handicap$wide(CallbackInfoReturnable<String> cir) {
		if (Prosopagnosia.hides(profile.getId())) {
			cir.setReturnValue("default");
		}
	}
}
