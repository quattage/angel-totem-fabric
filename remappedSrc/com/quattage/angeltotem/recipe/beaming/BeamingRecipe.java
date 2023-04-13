package com.quattage.angeltotem.recipe.beaming;

import com.quattage.angeltotem.recipe.InWorldFakeInventory;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BeamingRecipe implements Recipe<InWorldFakeInventory> {

    public static class BeamingRecipeType implements RecipeType<BeamingRecipe> {
        private BeamingRecipeType() {}
        public static final BeamingRecipeType INSTANCE = new BeamingRecipeType();
        public static final String ID = "beaming";

        public String toString() {
            return ID;
        }
    }
    protected final Ingredient inputIngredient;
    protected final ItemStack outputStack;
    protected final int time;
    protected final Identifier id;

    public static RecipeType<BeamingRecipe> Type = new BeamingRecipeType();

    protected BeamingRecipe(Ingredient inputIngredient, ItemStack outputStack, int time, Identifier id) {
        this.inputIngredient = inputIngredient;
        this.outputStack = outputStack;
        this.time = time;
        this.id = id;
    }
    
    public int getProcessTime() {
        return time;
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
        return BeamingRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return BeamingRecipeType.INSTANCE;
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
        return result;
    }
}

