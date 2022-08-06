package com.quattage.angeltotem.compat;

import com.quattage.angeltotem.AngelTotem;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class TrinketTotem implements Trinket {
    public static boolean isTrinketEquipped = false;

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        isTrinketEquipped = true;
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        isTrinketEquipped = false;
    }
    
    public static void dropTrinketTotem(PlayerEntity player, World world) {
        TrinketsApi.getTrinketComponent((LivingEntity) player).ifPresent(trinkets -> trinkets.forEach((reference, stack) -> {
            if(TrinketsApi.getTrinket(stack.getItem()) == TrinketsApi.getTrinket(AngelTotem.ANGEL_TOTEM)) {
                TrinketInventory trinketInventory = reference.inventory();
                trinketInventory.setStack(reference.index(), ItemStack.EMPTY);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
        }));
    }
}

