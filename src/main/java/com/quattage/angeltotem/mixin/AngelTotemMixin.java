package com.quattage.angeltotem.mixin;

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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
        PlayerEntity player = (PlayerEntity) (Object) this;
        PlayerInventory activeInventory = player.getInventory();
        
        //player.sendMessage(Text.of("flySpeed: " + this.abilities.flying), true);

        if(!this.abilities.creativeMode) {
            if(this.getAttacker() != null) {
                this.setAttacker(null);
                if(this.abilities.flying == true) {
                    World currentWorld = this.getWorld();
                    double playerX = player.getX();
                    double playerY = player.getY();
                    double playerZ = player.getZ();
                    if(currentOffHand == AngelTotem.ANGEL_TOTEM) {
                        activeInventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                        this.dropItem(AngelTotem.ANGEL_TOTEM);
                        currentWorld.playSound(null, playerX, playerY, playerZ, SoundEvents .ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
                    }
                    if(currentMainHand == AngelTotem.ANGEL_TOTEM) {
                        activeInventory.removeStack(activeInventory.selectedSlot);
                        this.dropItem(AngelTotem.ANGEL_TOTEM);
                        currentWorld.playSound(null, playerX, playerY, playerZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
                    }
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
