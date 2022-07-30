package com.quattage.angeltotem.mixin;


import org.lwjgl.system.CallbackI.P;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class AngelTotemMixin extends LivingEntity {

    @Shadow public abstract void sendAbilitiesUpdate();
    @Shadow @Final private PlayerAbilities abilities;
    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    public AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {

        Item currentOffHand = this.getOffHandStack().getItem();
        Item currentMainHand = this.getMainHandStack().getItem();
        LivingEntity attacker = this.getAttacker();
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory activeInventory = player.getInventory();
        if(!this.abilities.creativeMode) {
            this.sendMessage(Text.of("oh no: " + attacker), true);
            if(attacker != null) {
                this.setAttacker(null);
                if(currentOffHand == AngelTotem.ANGEL_TOTEM) {
                    activeInventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                    this.dropItem(AngelTotem.ANGEL_TOTEM);
                }
                if(currentMainHand == AngelTotem.ANGEL_TOTEM) {
                    activeInventory.removeStack(activeInventory.selectedSlot);
                    this.dropItem(AngelTotem.ANGEL_TOTEM);
                }
            }
            if(currentOffHand == AngelTotem.ANGEL_TOTEM || currentMainHand == AngelTotem.ANGEL_TOTEM)  {
                this.abilities.allowFlying = true; 
            } 
            else {
                this.abilities.allowFlying = false;
            }
        }
        this.sendAbilitiesUpdate();
    }   
}
