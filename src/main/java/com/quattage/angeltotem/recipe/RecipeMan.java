package com.quattage.angeltotem.recipe;

import java.util.Optional;
import com.quattage.angeltotem.recipe.beaming.BeamingRecipe;
import com.quattage.angeltotem.recipe.striking.StrikingRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RecipeMan {

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

	public static ItemStack parseBeamingRecipe(ItemStack inputItem, World world) {
        InWorldFakeInventory recipeManager = new InWorldFakeInventory(ItemStack.EMPTY);
        recipeManager.setStack(0, inputItem);
        Optional<BeamingRecipe> recipe = world.getRecipeManager()
                .getFirstMatch(BeamingRecipe.BeamingRecipeType.INSTANCE, recipeManager, world);
        if (recipe.isPresent()) {
            return recipe.get().craft(recipeManager);
        }
        return null;
    }
}
