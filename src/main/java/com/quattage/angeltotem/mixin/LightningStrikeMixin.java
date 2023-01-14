package com.quattage.angeltotem.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.recipe.RecipeMan;

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
        if (!this.world.isClient() && ((Object) this instanceof ItemEntity)) {
            ItemStack result = null;
            Vec3d strikePosition = this.getPos();
            if ((ItemEntity) (Object) this != null)
                result = RecipeMan.parseStrikingRecipe(((ItemEntity) (Entity) this).getStack(), world);
            if (result != null) {
                int resultCount = result.getCount();
                for (int itemCount = 0; itemCount < resultCount; itemCount++) {    
                    ItemStack resultToSpawn = result;
                    resultToSpawn.setCount(1);
                    ItemEntity newItem = new ItemEntity(world, strikePosition.x, strikePosition.y, strikePosition.z, resultToSpawn.copy());
                    newItem.setInvulnerable(true);
                    newItem.setVelocity((new Random().nextDouble() - 0.5) * 0.3d, 0.4d, (new Random().nextDouble() - 0.5) * 0.3d);
                    world.spawnEntity(newItem);
                }
                world.playSound(null, strikePosition.x, strikePosition.y, strikePosition.z, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.HOSTILE, 4f, 0.8f);
                this.remove(RemovalReason.DISCARDED);
            } else {
                super.onStruckByLightning(world, lightning);
            }
        } else {
            super.onStruckByLightning(world, lightning);
        }
    }
}