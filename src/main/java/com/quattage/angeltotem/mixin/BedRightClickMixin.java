package com.quattage.angeltotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.block.BlockState;
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

    // this mixin is responsible for overriding the action result when the player interacts with a bed
    // it just checks if the player is is holidng a totem while right clicking a bed and cancels the event
    // to prevent the player from accidentally sleeping while trying to bind a totem

    // This technically applies to every block tagged as a valid totem target, not just beds. For example, if you wanted a furnace to be a totem target,
    // this mixin would prevent the player from opening the furnace GUI when holding a totem.
    
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
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if(state.isIn(AngelTotem.getValidTotemTargets()) && player.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
            player.getMainHandStack().getItem().useOnBlock(new ItemUsageContext(player, hand, hitResult));
            cinfo.setReturnValue(ActionResult.PASS);
        }
    }
}


