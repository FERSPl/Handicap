package com.handicap.client;

import com.handicap.HandicapMod;
import com.handicap.WheelchairEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class WheelchairRenderer extends LivingEntityRenderer<WheelchairEntity, WheelchairModel> {
	private static final ResourceLocation TEXTURE = HandicapMod.id("textures/entity/wheelchair.png");

	public WheelchairRenderer(EntityRendererProvider.Context context) {
		super(context, new WheelchairModel(context.bakeLayer(WheelchairModel.LAYER)), 0.5f);
	}

	@Override
	public ResourceLocation getTextureLocation(WheelchairEntity entity) {
		return TEXTURE;
	}

	@Override
	protected boolean shouldShowName(WheelchairEntity entity) {
		return entity.hasCustomName() && super.shouldShowName(entity);
	}
}
