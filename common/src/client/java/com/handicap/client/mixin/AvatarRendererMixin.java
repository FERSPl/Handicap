package com.handicap.client.mixin;

import com.handicap.client.CastLayer;
import com.handicap.client.CastRenderer;
import com.handicap.client.ClientLimbs;
import com.handicap.client.Prosopagnosia;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
	@Unique
	private boolean handicap$slim;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void handicap$addCast(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
		handicap$slim = slim;
		AvatarRenderer<?> self = (AvatarRenderer<?>) (Object) this;
		((LivingEntityRendererAccessor) (Object) this).handicap$addLayer(new CastLayer(self, slim));
	}

	@Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
	private void handicap$hideName(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (Prosopagnosia.active() && mc.player != null && state.id != mc.player.getId()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderHand", at = @At("TAIL"))
	private void handicap$castInHand(PoseStack poseStack, SubmitNodeCollector collector, int light, Identifier skin,
			ModelPart arm, boolean sleeve, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		int limbs = ClientLimbs.get(mc.player.getUUID());
		PlayerModel model = ((AvatarRenderer<?>) (Object) this).getModel();
		CastRenderer.submit(poseStack, collector, light, arm, limbs, arm == model.rightArm, handicap$slim);
	}
}
