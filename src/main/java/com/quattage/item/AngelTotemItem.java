package com.quattage.item;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class AngelTotemItem extends Item {
    public AngelTotemItem(Settings settings) {
        super(settings);
    }
    
    @Override
    // called when a block is right clicked with this item
    public ActionResult useOnBlock(ItemUsageContext context) {
        // only execute by the server 
        World world = context.getWorld();
        if(!world.isClient()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) context.getPlayer();
            PlayerEntity player = context.getPlayer();
            BlockPos hitPosition = context.getBlockPos();
            BlockState hitState = world.getBlockState(hitPosition);
            // get the item in the player's main hand
            if(player.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
                // if they hit a bed
                if(hitState.getBlock() == Blocks.RED_BED) {
                    // store the x, y, and z blockpos of the bed as NBT in a new BoundAngelTotemItem item and give that to the player
                    // also remove the original AngelTotemItem from the player's inventory
                    PlayerInventory inventory = player.getInventory();
                    ItemStack result = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                    NbtCompound nbt = new NbtCompound();
                    nbt.putDouble("PositionX", hitPosition.getX());
                    nbt.putDouble("PositionY", hitPosition.getY());
                    nbt.putDouble("PositionZ", hitPosition.getZ());
                    String dimension = serverPlayer.getWorld().getRegistryKey().toString();
                    context.getPlayer().sendMessage(Text.of("butt fart: " + dimension), false);
                    nbt.putString("Dimension", dimension);
                    result.setNbt(nbt);
                    inventory.removeStack(inventory.selectedSlot);
                    inventory.setStack(inventory.selectedSlot, result);
                }
            }
        }
        return ActionResult.PASS;
    }

    public void forceUseOnBlock(World world, BlockPos pos, PlayerEntity player) {
        if(!world.isClient()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            BlockState hitState = world.getBlockState(pos);
            if(player.getMainHandStack().getItem() == AngelTotem.ANGEL_TOTEM) {
                if(hitState.getBlock() == Blocks.RED_BED) {
                    PlayerInventory inventory = player.getInventory();
                    ItemStack result = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                    NbtCompound nbt = new NbtCompound();
                    nbt.putDouble("PositionX", pos.getX());
                    nbt.putDouble("PositionY", pos.getY());
                    nbt.putDouble("PositionZ", pos.getZ());
                    String dimension = serverPlayer.getWorld().getRegistryKey().toString();
                    player.sendMessage(Text.of("butt fart: " + dimension), false);
                    nbt.putString("Dimension", dimension);
                    result.setNbt(nbt);
                    inventory.removeStack(inventory.selectedSlot);
                    inventory.setStack(inventory.selectedSlot, result);
                }
            }
        }
    }
}