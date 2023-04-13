package com.quattage.angeltotem.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import com.quattage.angeltotem.recipe.RecipeMan;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPreserveMixin extends Entity {

    public ItemEntityPreserveMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Override
    public void onBlockCollision(BlockState state) {
        // only run the following code from the server if the entity passed by the mixin is an ItemEntity
        if(((Object) this instanceof ItemEntity)) {
            // grab and store the entity instance cast to an ItemEntity
            ItemEntity activeItem = (ItemEntity) (Entity) this;
            // if the item in question can despawn in the first place
            if(activeItem.getItemAge() != Short.MIN_VALUE) {
                // if the stored activeItem passes the recipe lookup from the serializer 
                if(RecipeMan.parseStrikingRecipe(activeItem.getStack(), world) != null && state.getBlock() == Blocks.LIGHTNING_ROD) {
                    // if the blockstate passed by the override is a lightning rod
                    if(state.getBlock() == Blocks.LIGHTNING_ROD) {
                        if(!world.isClient()) { 
                            // set the ItemEntity to never despawn
                            activeItem.setNeverDespawn();
                            // play cool sound haha
                            if(!world.isThundering()) {
                                world.playSound(null, activeItem.getX(), activeItem.getY(), activeItem.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 2f, 0.8f);
                            }
                        } else {
                            // add particles for the client oo pretty
                            if(world.isThundering())
                            world.addParticle(ParticleTypes.GLOW, activeItem.getX(), activeItem.getY() + 0.2, activeItem.getZ(), (new Random().nextDouble() - 0.5) * 0.3d, 1d, (new Random().nextDouble() - 0.5) * 0.3d);
                            else
                            world.addParticle(ParticleTypes.SMOKE, activeItem.getX(), activeItem.getY() + 0.1, activeItem.getZ(), (new Random().nextDouble() - 0.5) * 0.1d, 0d, (new Random().nextDouble() - 0.5) * 0.1d);
                        }
                    }
                } else {
                    // if the previous conditions are not met, then fall back on the super's implementation 
                    super.onBlockCollision(state);
                    
                }
            } else {
                super.onBlockCollision(state);
            }
        }
        else {
            super.onBlockCollision(state);
        }
    }
}
