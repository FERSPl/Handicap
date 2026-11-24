package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererTiltMixin {
	@Inject(method = "bobHurt", at = @At("HEAD"))
	private void handicap$scoliosis(PoseStack poseStack, float partialTick, CallbackInfo ci) {
		if (ClientState.has(Handicap.SCOLIOSIS)) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(-12.0f));
		}
	}
}
