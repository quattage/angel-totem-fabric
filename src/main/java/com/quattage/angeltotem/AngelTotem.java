package com.quattage.angeltotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quattage.angeltotem.compat.TrinketsCompat;
import com.quattage.angeltotem.config.AngelTotemConfig;

import com.quattage.angeltotem.recipe.InWorldFakeInventory;

import com.quattage.angeltotem.recipe.beaming.BeamingRecipe;
import com.quattage.angeltotem.recipe.beaming.BeamingRecipeSerializer;
import com.quattage.angeltotem.recipe.beaming.BeamingRecipe.BeamingRecipeType;

import com.quattage.angeltotem.recipe.striking.StrikingRecipe;
import com.quattage.angeltotem.recipe.striking.StrikingRecipeSerializer;
import com.quattage.angeltotem.recipe.striking.StrikingRecipe.StrikingRecipeType;

import com.quattage.item.AngelTotemItem;
import com.quattage.item.BoundAngelTotemItem;

import me.shedaniel.autoconfig.ConfigHolder;

public class AngelTotem implements ModInitializer {
	
	public static final String MODID = "angeltotem";
	public static final ConfigHolder<AngelTotemConfig> CONFIG_HOLDER = AngelTotemConfig.initializeConfigs();


    public static AngelTotemConfig getConfig() {
        return CONFIG_HOLDER.getConfig();
    }

	

	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final TagKey<Block> TOTEM_TARGETS = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "totem_binding_targets"));
	public static final TagKey<Block> TOTEM_TARGETS_HARD = TagKey.of(Registry.BLOCK_KEY, new Identifier(MODID, "totem_binding_targets_hard"));

	public static final AngelTotemItem ANGEL_TOTEM = new AngelTotemItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).fireproof());
	public static final BoundAngelTotemItem BOUND_ANGEL_TOTEM = new BoundAngelTotemItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1).fireproof().rarity(Rarity.RARE));
	public static final Item TOTEM_FRAGMENT = new Item(new FabricItemSettings().group(ItemGroup.MISC).maxCount(16).fireproof());

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
		Registry.register(Registry.ITEM, new Identifier(MODID, "bound_totem_of_unfalling"), BOUND_ANGEL_TOTEM);
		Registry.register(Registry.ITEM, new Identifier(MODID, "totem_fragment"), TOTEM_FRAGMENT);
	}

	private void registerRecipes() {
		Registry.register(Registry.RECIPE_SERIALIZER, StrikingRecipeSerializer.ID, StrikingRecipeSerializer.INSTANCE);
		Identifier strikingTypeIdentifier =  new Identifier(MODID, StrikingRecipe.StrikingRecipeType.INSTANCE.toString());
		Registry.register(Registry.RECIPE_TYPE, strikingTypeIdentifier, StrikingRecipeType.INSTANCE);

		Registry.register(Registry.RECIPE_SERIALIZER, BeamingRecipeSerializer.ID, BeamingRecipeSerializer.INSTANCE);
		Identifier beamingTypeIdentifier =  new Identifier(MODID, BeamingRecipe.BeamingRecipeType.INSTANCE.toString());
		Registry.register(Registry.RECIPE_TYPE, beamingTypeIdentifier, BeamingRecipeType.INSTANCE);
	}

	public static TagKey<Block> getValidTotemTargets() {
		if(getConfig().BasicTotemOptions.hardMode) 
			return TOTEM_TARGETS_HARD;
		return TOTEM_TARGETS; 
	}

	public static boolean getShouldUseTrinkets() {
		return FabricLoader.getInstance().isModLoaded("trinkets") && getConfig().BasicTotemOptions.useTrinkets;
	}
	
	public static void messageLog(String message) {
		LOGGER.info(message);
	}

	public static ItemStack parseStrikingRecipe(ItemStack inputItem, World world) {
        // make a recipeManager object of type InWorldFakeInventory
        InWorldFakeInventory recipeManager = new InWorldFakeInventory(ItemStack.EMPTY);
        // set the fake inventory's "stack" to the provided input item
        recipeManager.setStack(0, inputItem);
        // create a new optional StrikingRecipe from the recipe manager, and then get
        // the first occurance of the recipe's use as defined by datapacks
        Optional<StrikingRecipe> recipe = world.getRecipeManager()
                .getFirstMatch(StrikingRecipe.StrikingRecipeType.INSTANCE, recipeManager, world);
        // if the optional striking recipe was found, return it
        if (recipe.isPresent()) {
            return recipe.get().craft(recipeManager);
        }
        // return null if the recipe was not found
        return null;
    }
}