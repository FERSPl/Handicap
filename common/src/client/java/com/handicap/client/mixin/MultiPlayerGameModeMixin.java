package com.handicap.client.mixin;

import com.handicap.HandicapRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
	private void handicap$shortReach(CallbackInfoReturnable<Float> cir) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && HandicapRules.isShort(mc.player)) {
			cir.setReturnValue(cir.getReturnValue() - 1.0f);
		}
	}
}
