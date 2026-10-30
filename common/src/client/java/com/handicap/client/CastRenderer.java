package com.handicap.client;

import com.handicap.HandicapMod;
import com.handicap.LimbsPayload;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public final class CastRenderer {
	private static final Identifier TEXTURE = HandicapMod.id("textures/entity/cast.png");
	private static final CubeDeformation PLASTER = new CubeDeformation(0.55f);

	private static ModelPart rightWide;
	private static ModelPart leftWide;
	private static ModelPart rightSlim;
	private static ModelPart leftSlim;

	private CastRenderer() {
	}

	public static void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ModelPart arm,
			int limbs, boolean right, boolean slim) {
		int bit = right ? LimbsPayload.CAST_RIGHT_ARM : LimbsPayload.CAST_LEFT_ARM;
		if ((limbs & bit) == 0 || !arm.visible) {
			return;
		}
		poseStack.pushPose();
		arm.translateAndRotate(poseStack);
		collector.submitModelPart(part(right, slim), poseStack, RenderTypes.entityCutout(TEXTURE), light,
			OverlayTexture.NO_OVERLAY, null);
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
