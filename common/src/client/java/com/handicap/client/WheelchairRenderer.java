package com.handicap.client;

import com.handicap.HandicapMod;
import com.handicap.WheelchairEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class WheelchairRenderer extends LivingEntityRenderer<WheelchairEntity, LivingEntityRenderState, WheelchairModel> {
	private static final ResourceLocation TEXTURE = HandicapMod.id("textures/entity/wheelchair.png");

	public WheelchairRenderer(EntityRendererProvider.Context context) {
		super(context, new WheelchairModel(context.bakeLayer(WheelchairModel.LAYER)), 0.5f);
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	protected boolean shouldShowName(WheelchairEntity entity, double distanceSq) {
		return entity.hasCustomName() && super.shouldShowName(entity, distanceSq);
	}
}
