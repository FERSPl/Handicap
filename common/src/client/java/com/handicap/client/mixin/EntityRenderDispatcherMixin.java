package com.handicap.client.mixin;

import com.handicap.client.Prosopagnosia;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@ModifyVariable(method = "extractEntity", at = @At("HEAD"), argsOnly = true)
	private Entity handicap$disguise(Entity entity) {
		return Prosopagnosia.disguise(entity);
	}
}
