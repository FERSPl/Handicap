package com.handicap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

/** Places a wheelchair on the clicked block, facing the same way as the player. */
public class WheelchairItem extends Item {
	public WheelchairItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level)) {
			return InteractionResult.SUCCESS;
		}
		WheelchairEntity chair = HandicapContent.wheelchair().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
		if (chair == null) {
			return InteractionResult.FAIL;
		}
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		Vec3 spot = Vec3.atBottomCenterOf(pos);
		float yaw = context.getRotation();
		chair.snapTo(spot.x, spot.y, spot.z, yaw, 0.0f);
		chair.setYHeadRot(yaw);
		chair.setYBodyRot(yaw);
		if (!level.noCollision(chair, chair.getBoundingBox())) {
			return InteractionResult.FAIL;
		}
		level.addFreshEntity(chair);
		context.getItemInHand().consume(1, context.getPlayer());
		return InteractionResult.SUCCESS;
	}
}
