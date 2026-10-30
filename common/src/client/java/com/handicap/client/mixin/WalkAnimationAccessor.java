package com.handicap.client.mixin;

import net.minecraft.world.entity.WalkAnimationState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationAccessor {
	@Accessor("speedOld")
	float handicap$getSpeedOld();

	@Accessor("speedOld")
	void handicap$setSpeedOld(float value);

	@Accessor("speed")
	float handicap$getSpeed();

	@Accessor("speed")
	void handicap$setSpeed(float value);

	@Accessor("position")
	float handicap$getPosition();

	@Accessor("position")
	void handicap$setPosition(float value);
}
