package com.quattage.item;

import java.util.List;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            MutableText bindingTarget = Text.translatable(totemNbt.getString("BindingTarget"));
            bindingTarget.setStyle(Style.EMPTY.withColor(Formatting.BLUE));
            Vec3i targetPos = new Vec3i(totemNbt.getInt("PositionX"), totemNbt.getInt("PositionY"), totemNbt.getInt("PositionZ"));
            MutableText targetDimension = Text.translatable("angeltotem.dimensionKey." + totemNbt.getString("Dimension"));
            MutableText targetLocation = (MutableText) Text.of("§7x§9 " + targetPos.getX() + "§7, y§9 " +  targetPos.getY() + "§7, z§9 " + targetPos.getZ());
            targetDimension.setStyle(Style.EMPTY.withColor(Formatting.BLUE));
            tooltip.add(Text.translatable("angeltotem.tooltip.boundTo", bindingTarget, targetDimension));
            tooltip.add(targetLocation);
        } else {
            tooltip.add(Text.translatable("angeltotem.tooltip.invalid"));
        }
    }
}