package com.quattage.angeltotem.recipe.striking;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.recipe.InWorldFakeInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StrikingRecipe implements Recipe<InWorldFakeInventory> {

    public static class StrikingRecipeType implements RecipeType<StrikingRecipe> {
        private StrikingRecipeType() {}
        public static final StrikingRecipeType INSTANCE = new StrikingRecipeType();
        public static final String ID = "striking";

        public String toString() {
            return ID;
        }
    }
    protected final Ingredient inputIngredient;
    protected final ItemStack outputStack;
    protected final Identifier id;

    public static RecipeType<StrikingRecipe> Type = new StrikingRecipeType();

    protected StrikingRecipe(Ingredient inputIngredient, ItemStack outputStack, Identifier id) {
        this.inputIngredient = inputIngredient;
        this.outputStack = outputStack;
        this.id = id;
        AngelTotem.messageLog("RECIPE CONSTRUCTOR INITIALIZES AS " + outputStack.getCount());
    }
    
    public Ingredient getInput() {
        return inputIngredient;
    }

    @Override
    public boolean fits(int var1, int var2) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StrikingRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return StrikingRecipeType.INSTANCE;
    }

    @Override
    public ItemStack getOutput() {
        return outputStack;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean matches(InWorldFakeInventory fakeInventory, World world) {
        if(this.inputIngredient.test(fakeInventory.getStack(0))) {
            return true;
        }
        return false;
        
    }

    @Override
    public ItemStack craft(InWorldFakeInventory fakeInventory) {
        ItemStack result = this.getOutput().copy();
        AngelTotem.messageLog("RECIPE CRAFT() METHOD SEES " + result.getCount());
        return result;
    }
}

