package com.quattage.angeltotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

//@SuppressWarnings("unused")
@Mixin(ServerPlayerInteractionManager.class)
public abstract class BedRightClickMixin{

    
    // actionresult check for right clicking on a bed pulled from the interactBLock() method in ServerPlayerInteractionManager
    // this mixin just makes it so that if the player is holding an unbound totem, right clicking on a bed fails
    // by doing this, it ensures that the player can properly bind a totem without sleeping in the bed they're trying to bind it to
    @Inject(method = "interactBlock",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;shouldCancelInteraction()Z",
                    ordinal = 0
            ),
            cancellable = true,
            locals = LocalCapture.NO_CAPTURE
    )
    public void shouldUseBed(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cinfo) {
        // store the state of the block in question
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        // if the player interacted with a bed and is holding a totem
        if(state.getBlock() == Blocks.RED_BED && player.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
            player.getMainHandStack().getItem().useOnBlock(new ItemUsageContext(player, hand, hitResult));
            cinfo.setReturnValue(ActionResult.PASS);
        }
    }
}


