package com.quattage.angeltotem.mixin;

import com.quattage.angeltotem.events.RightClickCallback;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.block.BedBlock;

@Mixin(BedBlockEntity.class)
public class BedRightClickMixin {
 
    @Inject(at = @At(value = "INVOKE", target = "Lnet.minecraft.block.entity.BedBlockEntity"), method = "interactBed", cancellable = true)
    private void onShear(final PlayerEntity player, final Hand hand, final CallbackInfoReturnable<Boolean> info) {
        ActionResult result = RightClickCallback.RCLICKEVENT.invoker().interact(player, (BedBlockEntity) (Object) this);
 
        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}

//CHANCE BEDBLOCKENTITY TO BEDBLOCK IDIOT