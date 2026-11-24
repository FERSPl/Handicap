package com.handicap.mixin;

import com.handicap.HandicapRules;

import net.minecraft.world.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRulesMixin {
	@Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
	private void handicap$jump(CallbackInfoReturnable<Float> cir) {
		float factor = HandicapRules.jumpFactor((LivingEntity) (Object) this);
		if (factor != 1.0f) {
			cir.setReturnValue(cir.getReturnValue() * factor);
		}
	}

	@Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
	private void handicap$fall(float fallDistance, float multiplier, CallbackInfoReturnable<Integer> cir) {
		float factor = HandicapRules.fallFactor((LivingEntity) (Object) this);
		if (factor != 1.0f) {
			cir.setReturnValue((int) Math.ceil(cir.getReturnValue() * factor));
		}
	}
}
