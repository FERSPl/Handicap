package com.handicap.client.mixin;

import com.handicap.client.Prosopagnosia;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
	private void handicap$steve(CallbackInfoReturnable<ResourceLocation> cir) {
		if (Prosopagnosia.hides(((AbstractClientPlayer) (Object) this).getUUID())) {
			cir.setReturnValue(DefaultPlayerSkin.getDefaultSkin());
		}
	}

	@Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
	private void handicap$wide(CallbackInfoReturnable<String> cir) {
		if (Prosopagnosia.hides(((AbstractClientPlayer) (Object) this).getUUID())) {
			cir.setReturnValue("default");
		}
	}
}
