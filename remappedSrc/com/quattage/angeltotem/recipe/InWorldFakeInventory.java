package com.quattage.angeltotem.recipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class InWorldFakeInventory implements Inventory {

    public ItemStack inputStack;

    public InWorldFakeInventory(ItemStack inputStack) {
        this.inputStack = inputStack;
    }

    @Override
    public void clear() {
        this.inputStack = ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStack(int var1) {
        return this.inputStack;
    }

    @Override
    public ItemStack removeStack(int var1, int var2) {
        return null;
    }

    @Override
    public ItemStack removeStack(int count) {
        this.inputStack.decrement(count);
        return this.inputStack;
    }

    @Override
    public void setStack(int var1, ItemStack inputStack) {
        this.inputStack = inputStack;
    }

    @Override
    public void markDirty() {
        
    }

    @Override
    public boolean canPlayerUse(PlayerEntity var1) {
        return true;
    }
    
}