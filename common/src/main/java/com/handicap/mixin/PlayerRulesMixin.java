package com.handicap.mixin;

import com.handicap.HandicapRules;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerRulesMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void handicap$mining(BlockState state, CallbackInfoReturnable<Float> cir) {
		float factor = HandicapRules.breakFactor((Player) (Object) this);
		if (factor != 1.0f) {
			cir.setReturnValue(cir.getReturnValue() * factor);
		}
	}

	@Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
	private void handicap$size(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if (HandicapRules.isShort((Player) (Object) this)) {
			cir.setReturnValue(cir.getReturnValue().scale(HandicapRules.SHORT_SCALE));
		}
	}

	@Inject(method = "getStandingEyeHeight", at = @At("RETURN"), cancellable = true)
	private void handicap$eyes(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
		if (HandicapRules.isShort((Player) (Object) this)) {
			cir.setReturnValue(cir.getReturnValue() * HandicapRules.SHORT_SCALE);
		}
	}
}
