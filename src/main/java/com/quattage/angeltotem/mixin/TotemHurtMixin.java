package com.quattage.angeltotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.compat.TrinketTotem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

//@SuppressWarnings("unused")
@Mixin(PlayerEntity.class)
public abstract class TotemHurtMixin extends LivingEntity {
    protected TotemHurtMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    //for the record: this sucks and i hate it.
    @Inject(at = @At("TAIL"), method = "applyDamage")
    protected void applyDamage(CallbackInfo info) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Boolean trinketEquip = false;

        if(AngelTotem.getShouldUseTrinkets()) 
            trinketEquip = TrinketTotem.isTrinketEquipped;
        else 
            trinketEquip = false;

        if(player.getAbilities().flying) {
            PlayerInventory inventory = ((PlayerEntity) (Object) this).getInventory();
            if(this.getOffHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
            }
            if(this.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
            }
            if(trinketEquip) 
                TrinketTotem.dropTrinketTotem(player, world);
        }
    }
}


