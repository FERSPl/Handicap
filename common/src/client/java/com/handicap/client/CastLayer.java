package com.handicap.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;

public class CastLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
	private final boolean slim;

	public CastLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, boolean slim) {
		super(parent);
		this.slim = slim;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int light, AbstractClientPlayer player,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || player.isInvisible()) {
			return;
		}
		Entity entity = mc.level.getEntity(player.getId());
		int limbs = ClientLimbs.get((entity != null ? entity : player).getUUID());
		if (limbs == 0) {
			return;
		}
		PlayerModel<AbstractClientPlayer> model = getParentModel();
		CastRenderer.render(poseStack, buffers, light, model.rightArm, limbs, true, slim);
		CastRenderer.render(poseStack, buffers, light, model.leftArm, limbs, false, slim);
	}
}
