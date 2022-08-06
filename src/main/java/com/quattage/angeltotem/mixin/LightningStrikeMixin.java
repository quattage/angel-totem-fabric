package com.quattage.angeltotem.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
                result = AngelTotem.parseStrikingRecipe(((ItemEntity) (Entity) this).getStack(), world);
            AngelTotem.messageLog(
                    "STRIKINGRECIPE IS PASSED " + ((ItemEntity) (Entity) this).getStack() + " FROM LIGHTNING MIXIN");
            // check if the parsed recupe result actually exists for the given input object
            if (result != null) {
                int resultCount = result.getCount();
                // loop once for each item in the result's stack
                
                for (int itemCount = 0; itemCount < resultCount; itemCount++) {    
                    // create a new instance of the result ItemStack, but set its count to 1
                    ItemStack resultToSpawn = result;
                    resultToSpawn.setCount(1);
                    // create a new ItemEntity at the strike position and initialize it with a copy of the resultToSpawn item
                    ItemEntity newItem = new ItemEntity(world, strikePosition.x, strikePosition.y, strikePosition.z,
                            resultToSpawn.copy());
                    // make sure the result ItemEntity doesn't burn in the fire created by lightning
                    newItem.setInvulnerable(true);
                    // add velocity to the result ItemStack with some random velocity vectors
                    newItem.setVelocity((new Random().nextDouble() - 0.5) * 0.3d, 0.4d, (new Random().nextDouble() - 0.5) * 0.3d);
                    // sets particular this newItem instance to never despawn for convenience, and also to prevent it from stacking with other items during the random fling
                    newItem.setNeverDespawn();
                    // add the itemEntity to the world
                    world.spawnEntity(newItem);
                }
                world.playSound(null, strikePosition.x, strikePosition.y, strikePosition.z, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.HOSTILE, 4f, 0.8f);
                this.remove(RemovalReason.DISCARDED);
            } else {
                // if the conditions are not met, defer to the vanilla non-overriden method
                super.onStruckByLightning(world, lightning);
            }
        } else {
            // if the conditions are not met, defer to the vanilla non-overriden method
            super.onStruckByLightning(world, lightning);
        }
    }
}