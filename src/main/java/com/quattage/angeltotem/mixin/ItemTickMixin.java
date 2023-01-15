package com.quattage.angeltotem.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.recipe.RecipeMan;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
public abstract class ItemTickMixin extends Entity {
    protected ItemTickMixin(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
    }
    


    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        ItemEntity entity = (ItemEntity)(Object)this;
        World world = entity.getWorld();
        Vec3d pos;
        
        boolean isClient = false;
        if(world.isClient) {
            isClient = true;
        }

        ItemStack input = entity.getStack();
        ItemStack result = RecipeMan.parseBeamingRecipe(input, world);
        int processTime = RecipeMan.parseBeamingRecipeTime(input, world);
        AngelTotem.messageLog("item is ticking...");
        if(result != null) {
            AngelTotem.messageLog("ITEM IS A VALID BEAMING RECIPE");
            if(isInBeaconBeam(entity, world)) {
                entity.setNeverDespawn();
                pos = entity.getPos();
                ItemStack groundStack = entity.getStack();
                if(!groundStack.hasNbt()) {
                    if(!isClient) {
                        groundStack.setNbt(new NbtCompound());
                        groundStack.getNbt().putInt("angeltotem:beaming_progress", 1);
                    } 
                } else {
                    if(!isClient) {
                        groundStack.getNbt().putInt("angeltotem:beaming_progress", (groundStack.getNbt().getInt("angeltotem:beaming_progress")) + 1);
                        AngelTotem.messageLog("progress: " + groundStack.getNbt().getInt("angeltotem:beaming_progress"));
                        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 0.5f, 0.3f);  
                    } else
                    world.addParticle(ParticleTypes.GLOW, pos.x, pos.y + 0.2, pos.z, (new Random().nextDouble() - 0.5) * 0.3d, 1d, (new Random().nextDouble() - 0.5) * 0.3d);
                }

                if(groundStack.getNbt().getInt("angeltotem:beaming_progress") >= processTime) {

                    if(!isClient) {
                        entity.remove(RemovalReason.DISCARDED);   
                        ItemEntity newItem = new ItemEntity(world, pos.x, pos.y, pos.z, result.copy());
                        newItem.setInvulnerable(true);
                        newItem.setVelocity((new Random().nextDouble() - 0.5) * 0.3d, 0.4d, (new Random().nextDouble() - 0.5) * 0.3d);
                        world.spawnEntity(newItem);
                        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ENDER_EYE_DEATH, SoundCategory.BLOCKS, 2f, 0.4f);  
                    } else {
                        world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.2, pos.z, (new Random().nextDouble() - 0.5) * 0.3d, 1d, (new Random().nextDouble() - 0.5) * 0.3d);
                    }
                } else {
                    if(!isInBeaconBeam(entity, world)) {
                        if(groundStack.hasNbt()) {
                            if(groundStack.getNbt().getInt("angeltotem:beaming_progress") >= 0) {
                                groundStack.getNbt().putInt("angeltotem:beaming_progress", (groundStack.getNbt().getInt("angeltotem:beaming_progress")) - 1);
                            } else 
                                groundStack.getNbt().remove("angelTotem:beaming_progress");
                        }
                    }
                }
            }
        }
    }

    private boolean isInBeaconBeam(ItemEntity entity, World world) {
        int posX = (int)Math.floor(entity.getX());;
        int posZ = (int)Math.floor(entity.getZ());;
        int maxHeight = world.getTopY(Heightmap.Type.WORLD_SURFACE, posX, posZ);
        BlockPos.Mutable tempPos = new BlockPos.Mutable(posX, Math.min((int)Math.floor(entity.getY()), maxHeight), posZ);

        while(tempPos.getY() > 0) {
            tempPos.move(Direction.DOWN);
            BlockState thisBlockState = world.getBlockState(tempPos);
            if(thisBlockState.getOpacity(world, tempPos) >= 15 && thisBlockState.getBlock() != Blocks.BEDROCK) 
                return false;

            if(thisBlockState.getBlock() == Blocks.BEACON) {
                BlockEntity tempBlockEntity = world.getBlockEntity(tempPos);
                if(!(tempBlockEntity instanceof BeaconBlockEntity)) 
                    return false;

                    BeaconBlockEntity beacon = (BeaconBlockEntity)tempBlockEntity;
                    if(!beacon.getBeamSegments().isEmpty()) {
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }
}
