package com.handicap.client;

import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class CastRenderer {
	private static final ResourceLocation TEXTURE = HandicapMod.id("textures/entity/cast.png");
	private static final CubeDeformation PLASTER = new CubeDeformation(0.55f);

	private static ModelPart rightWide;
	private static ModelPart leftWide;
	private static ModelPart rightSlim;
	private static ModelPart leftSlim;

	private CastRenderer() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource buffers, int light, ModelPart arm,
			int limbs, boolean right, boolean slim) {
		int bit = right ? LimbsPayload.CAST_RIGHT_ARM : LimbsPayload.CAST_LEFT_ARM;
		if ((limbs & bit) == 0 || !arm.visible) {
			return;
		}
		poseStack.pushPose();
		arm.translateAndRotate(poseStack);
		VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
		part(right, slim).render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
	}

	private static ModelPart part(boolean right, boolean slim) {
		if (rightWide == null) {
			rightWide = build(-3.0f, 4.0f);
			leftWide = build(-1.0f, 4.0f);
			rightSlim = build(-2.0f, 3.0f);
			leftSlim = build(-1.0f, 3.0f);
		}
		if (slim) {
			return right ? rightSlim : leftSlim;
		}
		return right ? rightWide : leftWide;
	}

	private static ModelPart build(float x, float width) {
		MeshDefinition mesh = new MeshDefinition();
		mesh.getRoot().addOrReplaceChild("cast",
			CubeListBuilder.create().texOffs(0, 0).addBox(x, 3.0f, -2.0f, width, 8.0f, 4.0f, PLASTER),
			PartPose.ZERO);
		return LayerDefinition.create(mesh, 32, 32).bakeRoot();
	}
}
