package com.handicap.client.mixin;

import com.handicap.LimbsPayload;
import com.handicap.client.ClientLimbs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.model.player.PlayerModel")
public abstract class PlayerModelMixin {
	@Unique
	private int handicap$hidden = 0;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"), require = 0)
	private void handicap$hideLimbs(AvatarRenderState state, CallbackInfo ci) {
		int limbs = 0;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			Entity entity = mc.level.getEntity(state.id);
			if (entity != null) {
				limbs = ClientLimbs.get(entity.getUUID());
			}
		}
		PlayerModel model = (PlayerModel) (Object) this;
		handicap$apply(limbs, LimbsPayload.LEFT_ARM, model.leftArm, model.leftSleeve);
		handicap$apply(limbs, LimbsPayload.RIGHT_ARM, model.rightArm, model.rightSleeve);
		handicap$apply(limbs, LimbsPayload.LEFT_LEG, model.leftLeg, model.leftPants);
		handicap$apply(limbs, LimbsPayload.RIGHT_LEG, model.rightLeg, model.rightPants);
	}

	@Unique
	private void handicap$apply(int limbs, int bit, ModelPart main, ModelPart overlay) {
		boolean hide = (limbs & bit) != 0;
		boolean wasHidden = (handicap$hidden & bit) != 0;
		if (hide) {
			main.visible = false;
			overlay.visible = false;
			handicap$hidden |= bit;
		} else if (wasHidden) {
			main.visible = true;
			overlay.visible = true;
			handicap$hidden &= ~bit;
		}
	}
}
