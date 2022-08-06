
package com.quattage.angeltotem.recipe.striking;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.quattage.angeltotem.AngelTotem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StrikingRecipeSerializer implements RecipeSerializer<StrikingRecipe>{
    public static final Identifier ID = new Identifier(AngelTotem.MODID, "striking");
	public static final StrikingRecipeSerializer INSTANCE = new StrikingRecipeSerializer();

	public StrikingRecipeSerializer() {}
	//read from the network
    @Override
    public StrikingRecipe read(Identifier recipeId, PacketByteBuf packetBuffer) {
    	Ingredient input = Ingredient.fromPacket(packetBuffer);
		ItemStack output = packetBuffer.readItemStack();
		return new StrikingRecipe(input, output, recipeId);
    }

	//read from JSON file
	@Override
	public StrikingRecipe read(Identifier recipeId, JsonObject json) {
		StrikingRecipeFormat recipeAsJson = new Gson().fromJson(json, StrikingRecipeFormat.class);

		//throw an error of the item ins/outs are missing
		if(recipeAsJson.inputItem == null || recipeAsJson.outputItem == null) {
			throw new JsonSyntaxException("Recipe is missing a required attribute!");
		}
		//make sure the provided item output amount is at least 1
		if(recipeAsJson.outputAmount == 0) {
			recipeAsJson.outputAmount = 1;
		}
		//move on with recipe creation
		Ingredient inputIngredient = Ingredient.fromJson(recipeAsJson.inputItem);
		//set the output item to the one provided by the json while making sure that item actually exists
		Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(recipeAsJson.outputItem)).orElseThrow(() -> new JsonSyntaxException("Item " + recipeAsJson.outputItem + " does not exist."));
		//turn the registered output item into a proper ItemStack by instantiating a new one with an outputAmount 
		ItemStack outputItemStack = new ItemStack(outputItem, recipeAsJson.outputAmount);
		//return the newly read recipe as a StrikingRecipe object
		return new StrikingRecipe(inputIngredient, outputItemStack, recipeId);
		//💀 verbose moment 💀
	}

	//write to the network
    @Override
    public void write(PacketByteBuf packetBuffer, StrikingRecipe recipe) {
		recipe.getInput().write(packetBuffer);
		packetBuffer.writeItemStack(recipe.getOutput());
	}
	
	public ItemStack readOutput(JsonElement outputObject) {
		if(outputObject.isJsonObject() && outputObject.getAsJsonObject().has("item"))
			return ShapedRecipe.outputFromJson(outputObject.getAsJsonObject());
		return null;
	}

}

class StrikingRecipeFormat {
	JsonObject inputItem;
	String outputItem;
	int outputAmount;
}