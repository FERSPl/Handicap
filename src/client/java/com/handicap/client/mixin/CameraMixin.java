package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.Camera")
public abstract class CameraMixin {
	@Final
	@Shadow
	private Quaternionf rotation;

	@Inject(method = "alignWithEntity", at = @At("TAIL"), require = 0)
	private void handicap$tilt(CallbackInfo ci) {
		if (ClientState.has(Handicap.SCOLIOSIS)) {
			this.rotation.rotateZ((float) Math.toRadians(12.0));
		}
	}
}
