package com.quattage.angeltotem.compat;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

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

    
}
