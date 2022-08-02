package com.quattage.angeltotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quattage.angeltotem.compat.TrinketsCompat;
import com.quattage.angeltotem.config.AngelTotemConfig;

import me.shedaniel.autoconfig.ConfigHolder;

public class AngelTotem implements ModInitializer {
	
	public static final ConfigHolder<AngelTotemConfig> CONFIG_HOLDER = AngelTotemConfig.initializeConfigs();
    public static AngelTotemConfig getConfig() {
        return CONFIG_HOLDER.getConfig();
    }

	public static final Logger LOGGER = LoggerFactory.getLogger("angeltotem");
	public static final Item ANGEL_TOTEM = new Item(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).fireproof());
	
	@Override
	public void onInitialize() {
		LOGGER.info("Angel Totem coming to you live from Not Scottland, Minnesota");
		Registry.register(Registry.ITEM, new Identifier("angeltotem", "totem_of_unfalling"), ANGEL_TOTEM);
		if(isTrinketsLoaded()) {
			LOGGER.info("Trinkets detected!");
			if(getConfig().BasicTotemOptions.useTrinkets) {
				TrinketsCompat.initializeTrinketTotem();
				LOGGER.info("Totem registered as a trinket!");
			} else {
				LOGGER.info("Totem of Unfalling's trinket regsitry has been disabled!");
			}
		}
	}

	public boolean isTrinketsLoaded() {
		return FabricLoader.getInstance().isModLoaded("trinkets");
	}

	public static float clampValue(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
}