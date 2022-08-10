package com.quattage.angeltotem.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.quattage.angeltotem.AngelTotem;

@Mixin(ServerPlayerInteractionManager.class)
public class TotemRightClickMixin {
    @Inject(method = "interactBlock",
            at = @At(
                    value = "TAIL",
                    ordinal = 0
            ),
            cancellable = true
    )
    public void useTotemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> infoReturnable) {
        BlockPos hitPosition = hitResult.getBlockPos();
        BlockState hitState = world.getBlockState(hitPosition);
        if(player.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
            if(hitState.getBlock() == Blocks.GRASS_BLOCK) {
                PlayerInventory inventory = player.getInventory();
                ItemStack result = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                NbtCompound nbt = new NbtCompound();
                nbt.putInt("PositionX", hitPosition.getX());
                nbt.putInt("PositionY", hitPosition.getY());
                nbt.putInt("PositionZ", hitPosition.getZ());
                result.setNbt(nbt);
                inventory.removeStack(inventory.selectedSlot);
                inventory.setStack(inventory.selectedSlot, result);
            }
        }
    }
}