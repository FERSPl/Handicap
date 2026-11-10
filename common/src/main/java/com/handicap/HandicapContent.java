package com.handicap;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
	public static final Identifier WHEELCHAIR_ID = HandicapMod.id("wheelchair");
	public static final ResourceKey<EntityType<?>> WHEELCHAIR_ENTITY_KEY = ResourceKey.create(Registries.ENTITY_TYPE, WHEELCHAIR_ID);
	public static final ResourceKey<Item> WHEELCHAIR_ITEM_KEY = ResourceKey.create(Registries.ITEM, WHEELCHAIR_ID);
	public static final ResourceKey<CreativeModeTab> TOOLS_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
		Identifier.withDefaultNamespace("tools_and_utilities"));

	private static EntityType<WheelchairEntity> wheelchair;
	private static Item wheelchairItem;

	private HandicapContent() {
	}

	public static EntityType<WheelchairEntity> createEntity() {
		wheelchair = EntityType.Builder.of(WheelchairEntity::new, MobCategory.MISC)
			.sized(0.9f, 1.0f)
			.passengerAttachments(0.25f)
			.clientTrackingRange(10)
			.build(WHEELCHAIR_ENTITY_KEY);
		return wheelchair;
	}

	public static Item createItem() {
		wheelchairItem = new WheelchairItem(new Item.Properties().setId(WHEELCHAIR_ITEM_KEY).stacksTo(1));
		return wheelchairItem;
	}

	public static EntityType<WheelchairEntity> wheelchair() {
		return wheelchair;
	}

	public static Item wheelchairItem() {
		return wheelchairItem;
	}
}
