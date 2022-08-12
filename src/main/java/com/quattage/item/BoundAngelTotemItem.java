package com.quattage.item;

import java.util.List;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BoundAngelTotemItem extends Item {
    public BoundAngelTotemItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasNbt();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(stack.hasNbt()) {
            NbtCompound totemNbt = stack.getNbt();
            String bindingTarget = totemNbt.getString("BindingTarget");
            Vec3i targetPos = new Vec3i(totemNbt.getInt("PositionX"), totemNbt.getInt("PositionY"), totemNbt.getInt("PositionZ"));
            String targetDimension = totemNbt.getString("Dimension");
            tooltip.add(Text.of("Bound to " + bindingTarget + " in " + targetDimension));
        } else {
            tooltip.add(Text.of("§7This totem is §4invalid§7."));
        }
    }
}