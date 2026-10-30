package com.handicap.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;

public class CastLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private final boolean slim;

	public CastLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, boolean slim) {
		super(parent);
		this.slim = slim;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state,
			float yRot, float xRot) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || state.isInvisible) {
			return;
		}
		Entity entity = mc.level.getEntity(state.id);
		if (entity == null) {
			return;
		}
		int limbs = ClientLimbs.get(entity.getUUID());
		if (limbs == 0) {
			return;
		}
		PlayerModel model = getParentModel();
		CastRenderer.submit(poseStack, collector, light, model.rightArm, limbs, true, slim);
		CastRenderer.submit(poseStack, collector, light, model.leftArm, limbs, false, slim);
	}
}
