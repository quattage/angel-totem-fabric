package com.quattage.angeltotem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.compat.TrinketTotem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
            if(this.getMainHandStack().getItem() == AngelTotem.BOUND_ANGEL_TOTEM) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(inventory.selectedSlot).getNbt());
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(totemToDrop, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(this.getOffHandStack().getItem() == AngelTotem.BOUND_ANGEL_TOTEM) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getNbt());
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(totemToDrop, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(trinketEquip) 
                TrinketTotem.dropTrinketTotem(player, world);
        }
    }
}


