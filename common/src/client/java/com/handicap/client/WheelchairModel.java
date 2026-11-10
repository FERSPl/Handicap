package com.handicap.client;

import com.handicap.HandicapMod;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** Wheelchair model, exported from the Blockbench project (wheelchair.bbmodel, 128x128 texture). */
public class WheelchairModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(HandicapMod.id("wheelchair"), "main");

	private final ModelPart rightFrontWheel;
	private final ModelPart leftFrontWheel;
	private final ModelPart rightBackWheel;
	private final ModelPart leftBackWheel;

	public WheelchairModel(ModelPart root) {
		super(root);
		this.rightFrontWheel = root.getChild("right_front_wheel");
		this.leftFrontWheel = root.getChild("left_front_wheel");
		this.rightBackWheel = root.getChild("right_back_wheel");
		this.leftBackWheel = root.getChild("left_back_wheel");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition right_front_wheel = partdefinition.addOrReplaceChild("right_front_wheel", CubeListBuilder.create().texOffs(30, 43).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 23.0F, -4.5F));

		PartDefinition left_front_wheel = partdefinition.addOrReplaceChild("left_front_wheel", CubeListBuilder.create().texOffs(36, 43).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 23.0F, -4.5F));

		PartDefinition right_back_wheel = partdefinition.addOrReplaceChild("right_back_wheel", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, -4.5F, -4.5F, 2.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 2.5F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 3.5F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 1.5F, -4.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 2.5F, 2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 3.5F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, 1.5F, 3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -3.5F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -4.5F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -2.5F, -4.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -3.5F, 2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -4.5F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(-2.0F, -2.5F, 3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 19.5F, 5.0F));

		PartDefinition left_back_wheel = partdefinition.addOrReplaceChild("left_back_wheel", CubeListBuilder.create().texOffs(90, 0).addBox(0.0F, -4.5F, -4.5F, 2.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 2.5F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 3.5F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 1.5F, -4.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 2.5F, 2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 3.5F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, 1.5F, 3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -3.5F, -3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -4.5F, -2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -2.5F, -4.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -3.5F, 2.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -4.5F, 1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 43).addBox(0.0F, -2.5F, 3.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 19.5F, 5.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(34, 72).addBox(-6.0F, -7.0F, -5.0F, 12.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, 73).addBox(-7.0F, -16.0F, 5.0F, 14.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 4).addBox(-6.0F, -6.0F, -0.5F, 12.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 46).addBox(-7.0F, -11.0F, -11.0F, 1.0F, 10.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 47).addBox(6.0F, -11.0F, -11.0F, 1.0F, 10.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(10, 6).addBox(-6.0F, -17.0F, 5.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(10, 9).addBox(5.0F, -17.0F, 5.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(18, 0).addBox(-6.0F, -2.5F, -10.0F, 12.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(18, 11).addBox(6.0F, -3.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 11).addBox(-7.0F, -3.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 12).addBox(6.0F, -6.0F, -8.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 12).addBox(-7.0F, -6.0F, -8.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 0).addBox(6.0F, -11.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 0).addBox(-7.0F, -11.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 0).addBox(-6.0F, -16.0F, 5.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(10, 12).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 12).addBox(-13.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -2.0F, -10.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -15.0F, -1.0F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -1.0F, 5.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(10, 12).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 12).addBox(13.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -5.0F, -8.0F, -1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
		super.setupAnim(state);
		// The wheels turn as the chair rolls. The small casters spin faster.
		float roll = state.walkAnimationPos;
		rightBackWheel.xRot = roll * 0.6f;
		leftBackWheel.xRot = roll * 0.6f;
		rightFrontWheel.xRot = roll * 2.0f;
		leftFrontWheel.xRot = roll * 2.0f;
	}
}
