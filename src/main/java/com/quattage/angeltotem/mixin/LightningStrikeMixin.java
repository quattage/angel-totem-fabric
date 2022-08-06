package com.quattage.angeltotem.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;

import com.quattage.angeltotem.recipe.striking.StrikingRecipe;
import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.helpers.MathHelper;
import com.quattage.angeltotem.recipe.InWorldFakeInventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
public abstract class LightningStrikeMixin extends Entity {

    public LightningStrikeMixin(EntityType<? extends Entity> type, World world) {
        super(type, world);
    }
    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        // ensures this code is only executed by the server when the struck LivingEntity
        // is of subtype ItemEntity
        if (!this.world.isClient() && ((Object) this instanceof ItemEntity)) {
            // initialize an itemstack as null
            ItemStack result = null;
            // save the position of the lightning strike to use as a reference for where the
            // item gets spawned
            Vec3d strikePosition = this.getPos();
            // if the itemEntity isn't null, then parse the ingredient to get its result
            if ((ItemEntity) (Object) this != null)
                result = parseStrikingRecipe(((ItemEntity) (Entity) this).getStack());
            AngelTotem.messageLog(
                    "STRIKINGRECIPE IS PASSED " + ((ItemEntity) (Entity) this).getStack() + " FROM LIGHTNING MIXIN");
            // check if the parsed recupe result actually exists for the given input object
            if (result != null) {
                int resultCount = result.getCount();
                this.remove(RemovalReason.KILLED);
                // loop once for each item in the result's stack
                for (int itemCount = 0; itemCount < resultCount; itemCount++) {
                    AngelTotem.messageLog("SPAWNING " + itemCount + " OUT OF " + resultCount + ", doStrike is ");
                    // create a new instance of the result ItemStack, but set its count to 1
                    ItemStack resultToSpawn = result;
                    resultToSpawn.setCount(1);
                    // create a new ItemEntity at the strike position and initialize it with the
                    // resultToSpawn item
                    ItemEntity newItem = new ItemEntity(world, strikePosition.x, strikePosition.y, strikePosition.z,
                            resultToSpawn);
                    // make sure the result ItemEntity doesn't burn in the fire created by lightning
                    newItem.setInvulnerable(true);
                    // add velocity to the result ItemStack with some random velocity vectors
                    newItem.setVelocity(MathHelper.randomD(-0.15d, 0.15d), 0.5f, MathHelper.randomD(-0.15d, 0.15d));
                    // add the itemEntity to the world
                    world.spawnEntity(newItem);
                }
            } else {
                // if the conditions are not met, defer to the vanilla non-overriden method
                super.onStruckByLightning(world, lightning);
            }
        } else {
            // if the conditions are not met, defer to the vanilla non-overriden method
            super.onStruckByLightning(world, lightning);
        }
    }

    public ItemStack parseStrikingRecipe(ItemStack inputItem) {
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