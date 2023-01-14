package com.quattage.angeltotem.mixin;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        if(!world.isClient()) {
            ItemStack input = entity.getStack();
            ItemStack result = RecipeMan.parseBeamingRecipe(input, world);
            AngelTotem.messageLog("item is ticking...");
            if(result != null) {
                 AngelTotem.messageLog("ITEM IS A VALID BEAMING RECIPE");
                if(isInBeaconBeam(entity, world)) {
                    AngelTotem.messageLog("ITEM IN BEACON BEAM!!!!");
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
