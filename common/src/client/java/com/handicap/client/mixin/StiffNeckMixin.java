package com.handicap.client.mixin;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class StiffNeckMixin {
	@Unique
	private static final float MARGIN = 5.0f;

	@Inject(method = "turn", at = @At("TAIL"))
	private void handicap$stiffNeck(double yRot, double xRot, CallbackInfo ci) {
		Entity self = (Entity) (Object) this;
		if (self instanceof LocalPlayer && ClientState.has(Handicap.STIFF_NECK)) {
			self.setXRot(Mth.clamp(self.getXRot(), -MARGIN, MARGIN));
			self.xRotO = Mth.clamp(self.xRotO, -MARGIN, MARGIN);
		}
	}
}
