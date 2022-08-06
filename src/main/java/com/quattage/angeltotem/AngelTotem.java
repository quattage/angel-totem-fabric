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
import com.quattage.angeltotem.recipe.striking.StrikingRecipe;
import com.quattage.angeltotem.recipe.striking.StrikingRecipeSerializer;
import com.quattage.angeltotem.recipe.striking.StrikingRecipe.StrikingRecipeType;

import me.shedaniel.autoconfig.ConfigHolder;

public class AngelTotem implements ModInitializer {
	
	public static final String MODID = "angeltotem";
	public static final ConfigHolder<AngelTotemConfig> CONFIG_HOLDER = AngelTotemConfig.initializeConfigs();
    public static AngelTotemConfig getConfig() {
        return CONFIG_HOLDER.getConfig();
    }

	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final Item ANGEL_TOTEM = new Item(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).fireproof());
	
	@Override
	public void onInitialize() {
		messageLog("my butt hurts");
		registerItems();
		registerRecipes();

		if(getShouldUseTrinkets()) {
			messageLog("Trinkets Detected!");
			if(getConfig().BasicTotemOptions.useTrinkets) {
				TrinketsCompat.initializeTrinketTotem();
				LOGGER.info("Totem registered as a trinket!");
				messageLog("Totem registered as a trinket!");
			} else {
				messageLog("Totem of Unfalling's trinket regsitry has been disabled!");
			}
		}
	}

	private void registerItems() {
		Registry.register(Registry.ITEM, new Identifier(MODID, "totem_of_unfalling"), ANGEL_TOTEM);
	}

	private void registerRecipes() {
		Registry.register(Registry.RECIPE_SERIALIZER, StrikingRecipeSerializer.ID, StrikingRecipeSerializer.INSTANCE);
		Identifier typeIdentifier =  new Identifier(MODID, StrikingRecipe.StrikingRecipeType.INSTANCE.toString());
		messageLog("registering new recipe type: " + typeIdentifier);
		Registry.register(Registry.RECIPE_TYPE, typeIdentifier, StrikingRecipeType.INSTANCE);
	}

	public static boolean getShouldUseTrinkets() {
		return FabricLoader.getInstance().isModLoaded("trinkets") && getConfig().BasicTotemOptions.useTrinkets;
	}

	public static float clampValue(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	public static void messageLog(String message) {
		LOGGER.info(message);
	}
}