package com.quattage.angelrings;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AngelRings implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("angelrings");

	public static final Item ANGEL_RING = new Item(new FabricItemSettings().group(ItemGroup.TOOLS));
	
	@Override
	public void onInitialize() {
		LOGGER.info("Angel Rings coming to you live from Not Scottland, Minnesota");
		Registry.register(Registry.ITEM, new Identifier("angelrings", "angel_ring"), ANGEL_RING);
	}
}