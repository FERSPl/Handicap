package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void handicap$swapLeftRight(boolean sneaking, float sneakSpeed, CallbackInfo ci) {
		if (!ClientState.has(Handicap.SPATIAL_DISORIENTATION)) {
			return;
		}
		Input input = (Input) (Object) this;
		boolean left = input.left;
		input.left = input.right;
		input.right = left;
		input.leftImpulse = -input.leftImpulse;
	}
}
