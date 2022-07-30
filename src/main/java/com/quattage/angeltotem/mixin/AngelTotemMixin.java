package com.quattage.angeltotem.mixin;


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Item;
import net.minecraft.text.Texts;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class AngelTotemMixin extends LivingEntity {

    @Shadow public abstract void sendAbilitiesUpdate();
    @Shadow @Final private PlayerAbilities abilities;
    @Shadow public abstract void sendMessage(String string, boolean actionBar);
    
    private boolean canFly = true;

    public AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {

        Item currentOffHand = this.getOffHandStack().getItem();
        Item currentMainHand = this.getMainHandStack().getItem();
        LivingEntity attacker = this.getAttacker();

        this.sendMessage("attacker: " + attacker, false);

        if(!this.abilities.creativeMode) {
            if(canFly)
            {
                if(currentOffHand == AngelTotem.ANGEL_TOTEM || currentMainHand == AngelTotem.ANGEL_TOTEM)  {
                    this.abilities.allowFlying = true;
                } 
                else {
                    this.abilities.allowFlying = false;
                }
            }
            else {
                this.abilities.allowFlying = false;
            }
            this.sendAbilitiesUpdate();
        }
    }   
}
