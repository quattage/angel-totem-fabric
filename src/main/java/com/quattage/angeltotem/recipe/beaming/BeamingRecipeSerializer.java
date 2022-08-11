
package com.quattage.angeltotem.recipe.beaming;

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

public class BeamingRecipeSerializer implements RecipeSerializer<BeamingRecipe>{
    public static final Identifier ID = new Identifier(AngelTotem.MODID, "beaming");
	public static final BeamingRecipeSerializer INSTANCE = new BeamingRecipeSerializer();

	public BeamingRecipeSerializer() {}
	// read from the network
    @Override
    public BeamingRecipe read(Identifier recipeId, PacketByteBuf packetBuffer) {
    	Ingredient input = Ingredient.fromPacket(packetBuffer);
		ItemStack output = packetBuffer.readItemStack();
		int time  = packetBuffer.readInt();
		return new BeamingRecipe(input, output, time, recipeId);
    }

	// read from JSON file
	@Override
	public BeamingRecipe read(Identifier recipeId, JsonObject json) {
		BeamingRecipeFormat recipeAsJson = new Gson().fromJson(json, BeamingRecipeFormat.class);

		// throw an error of the item ins/outs are missing
		if(recipeAsJson.inputItem == null || recipeAsJson.outputItem == null) {
			throw new JsonSyntaxException("Recipe is missing a required attribute!");
		}
		// make sure the provided item output amount is at least 1
		if(recipeAsJson.outputAmount == 0) {
			recipeAsJson.outputAmount = 1;
		}
		// move on with recipe creation
		Ingredient inputIngredient = Ingredient.fromJson(recipeAsJson.inputItem);
		// set the output item to the one provided by the json while making sure that item actually exists
		Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(recipeAsJson.outputItem)).orElseThrow(() -> new JsonSyntaxException("Item " + recipeAsJson.outputItem + " does not exist."));
		// turn the registered output item into a proper ItemStack by instantiating a new one with an outputAmount 
		ItemStack outputItemStack = new ItemStack(outputItem, recipeAsJson.outputAmount);
		int time = recipeAsJson.processTime;
		// return the newly read recipe as a BeamingRecipe object
		return new BeamingRecipe(inputIngredient, outputItemStack, time, recipeId);
	}

	// write to the network
    @Override
    public void write(PacketByteBuf packetBuffer, BeamingRecipe recipe) {
		recipe.getInput().write(packetBuffer);
		packetBuffer.writeItemStack(recipe.getOutput());
		packetBuffer.writeInt(recipe.getProcessTime());
	}
	
	public ItemStack readOutput(JsonElement outputObject) {
		if(outputObject.isJsonObject() && outputObject.getAsJsonObject().has("item"))
			return ShapedRecipe.outputFromJson(outputObject.getAsJsonObject());
		return null;
	}

}

class BeamingRecipeFormat {
	JsonObject inputItem;
	String outputItem;
	int outputAmount;
	int processTime;
}