package com.handicap;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * Items and entities added by the mod. The objects are created by the loader inside its
 * registration phase (registries refuse new entries outside of it), through {@link #createEntity()}
 * and {@link #createItem()}.
 */
public final class HandicapContent {
	public static final ResourceLocation WHEELCHAIR_ID = HandicapMod.id("wheelchair");
	public static final ResourceKey<EntityType<?>> WHEELCHAIR_ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, WHEELCHAIR_ID);
	public static final ResourceKey<Item> WHEELCHAIR_ITEM_KEY = ResourceKey.create(Registries.ITEM, WHEELCHAIR_ID);
	public static final ResourceKey<CreativeModeTab> TOOLS_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
		new ResourceLocation("minecraft", "tools_and_utilities"));

	private static EntityType<WheelchairEntity> wheelchair;
	private static Item wheelchairItem;
	private static SoundEvent flatulenceSound;
	private static SoundEvent newHandicapSound;
	public static final ResourceLocation FLATULENCE_SOUND_ID = HandicapMod.id("flatulence");
	public static final ResourceLocation NEW_HANDICAP_SOUND_ID = HandicapMod.id("new_handicap");

	private HandicapContent() {
	}

	public static EntityType<WheelchairEntity> createEntity() {
		wheelchair = EntityType.Builder.of(WheelchairEntity::new, MobCategory.MISC)
			.sized(0.9f, 1.0f)
			.clientTrackingRange(10)
			.build(WHEELCHAIR_ID.toString());
		return wheelchair;
	}

	public static Item createItem() {
		wheelchairItem = new WheelchairItem(new Item.Properties().stacksTo(1));
		return wheelchairItem;
	}

	public static EntityType<WheelchairEntity> wheelchair() {
		return wheelchair;
	}

	public static SoundEvent createFlatulenceSound() {
		flatulenceSound = SoundEvent.createVariableRangeEvent(FLATULENCE_SOUND_ID);
		return flatulenceSound;
	}

	public static SoundEvent createNewHandicapSound() {
		newHandicapSound = SoundEvent.createVariableRangeEvent(NEW_HANDICAP_SOUND_ID);
		return newHandicapSound;
	}

	public static SoundEvent newHandicapSound() {
		return newHandicapSound;
	}

	public static SoundEvent flatulenceSound() {
		return flatulenceSound;
	}

	public static Item wheelchairItem() {
		return wheelchairItem;
	}
}
