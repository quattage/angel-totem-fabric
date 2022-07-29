package com.quattage.angeltotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AngelTotem implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("angeltotem");

	public static final Item ANGEL_TOTEM = new Item(new FabricItemSettings().group(ItemGroup.TOOLS));
	
	@Override
	public void onInitialize() {
		LOGGER.info("Angel Totem coming to you live from Not Scottland, Minnesota");
		Registry.register(Registry.ITEM, new Identifier("angeltotem", "angel_totem"), ANGEL_TOTEM);
	}
}