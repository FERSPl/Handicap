package com.handicap.client.mixin;

import com.handicap.HandicapRules;
import com.handicap.client.CastLayer;
import com.handicap.client.CastRenderer;
import com.handicap.client.ClientLimbs;
import com.handicap.client.Prosopagnosia;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class AvatarRendererMixin {
	@Unique
	private boolean handicap$slim;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void handicap$addCast(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
		handicap$slim = slim;
		PlayerRenderer self = (PlayerRenderer) (Object) this;
		((LivingEntityRendererAccessor) (Object) this).handicap$addLayer(new CastLayer(self, slim));
	}

	@Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
	private void handicap$hideName(AbstractClientPlayer player, Component name, PoseStack poseStack,
			MultiBufferSource buffers, int light, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (Prosopagnosia.active() && mc.player != null && player.getId() != mc.player.getId()) {
			ci.cancel();
		}
	}

	@Inject(method = "scale", at = @At("TAIL"))
	private void handicap$short(AbstractClientPlayer player, PoseStack poseStack, float partialTick, CallbackInfo ci) {
		if (HandicapRules.isShort(player)) {
			poseStack.scale(HandicapRules.SHORT_SCALE, HandicapRules.SHORT_SCALE, HandicapRules.SHORT_SCALE);
		}
	}

	@Inject(method = "renderHand", at = @At("TAIL"))
	private void handicap$castInHand(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
			ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
		int limbs = ClientLimbs.get(player.getUUID());
		PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
		CastRenderer.render(poseStack, buffers, light, arm, limbs, arm == model.rightArm, handicap$slim);
	}
}
