package com.quattage.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BoundAngelTotemItem extends Item {
    public BoundAngelTotemItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasNbt();
    }

    
}