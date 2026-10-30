package com.handicap.client.mixin;

import com.handicap.client.Prosopagnosia;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerSkin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
	private void handicap$steve(CallbackInfoReturnable<PlayerSkin> cir) {
		if (Prosopagnosia.hides(((AbstractClientPlayer) (Object) this).getUUID())) {
			cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin());
		}
	}
}
