package com.handicap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A wheelchair the player can sit in and drive. It climbs 1-block steps like a horse,
 * cannot jump, and breaks in one hit, dropping its item.
 */
public class WheelchairEntity extends LivingEntity {
	public WheelchairEntity(EntityType<? extends WheelchairEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.MOVEMENT_SPEED, 0.1)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		if (player.isSecondaryUseActive() || isVehicle()) {
			return InteractionResult.PASS;
		}
		if (!level().isClientSide()) {
			player.startRiding(this);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public LivingEntity getControllingPassenger() {
		return getFirstPassenger() instanceof Player player ? player : null;
	}

	@Override
	protected void tickRidden(Player player, Vec3 input) {
		super.tickRidden(player, input);
		setRot(player.getYRot(), 0.0f);
		yRotO = getYRot();
		yBodyRot = getYRot();
		yHeadRot = getYRot();
	}

	@Override
	protected Vec3 getRiddenInput(Player player, Vec3 input) {
		// A wheelchair cannot strafe; reversing is slower.
		float forward = player.zza;
		if (forward < 0.0f) {
			forward *= 0.5f;
		}
		return new Vec3(0.0, 0.0, forward);
	}

	@Override
	protected float getRiddenSpeed(Player player) {
		return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (isRemoved()) {
			return false;
		}
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			discard();
			return true;
		}
		if (source.getEntity() instanceof Player player) {
			if (!player.getAbilities().instabuild) {
				spawnAtLocation(level, new ItemStack(HandicapContent.wheelchairItem()));
			}
			ejectPassengers();
			discard();
			return true;
		}
		// Falls, fire, mobs...: a wheelchair is not alive.
		return false;
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(HandicapContent.wheelchairItem());
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return getPassengers().isEmpty();
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}
}
