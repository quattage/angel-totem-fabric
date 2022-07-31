package com.quattage.angeltotem.mixin;

import org.lwjgl.system.CallbackI.P;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@SuppressWarnings("unused")
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
        PlayerInventory activeInventory = ((PlayerEntity) (Object) this).getInventory();
        ServerPlayerEntity serverPlayer = null;
        BlockPos respawnPosition = null;
        boolean spawnPointHasBed = false;
        boolean sameDimension = false;
        World currentWorld = this.getWorld();
        if(!world.isClient()) {
                serverPlayer = (ServerPlayerEntity) (Object) this;
                respawnPosition = serverPlayer.getSpawnPointPosition();
                
                spawnPointHasBed = currentWorld.getBlockState(respawnPosition).getBlock() == Blocks.RED_BED;
                sameDimension = serverPlayer.getSpawnPointDimension() == currentWorld.getRegistryKey();

                if(spawnPointHasBed)
                    this.sendMessage(Text.of("spawn point bed? " + spawnPointHasBed + ", same dimension?" + sameDimension), false);
                else
                    this.sendMessage(Text.of("The previous bed has been removed."), false);
            

            if(!this.abilities.creativeMode) {
                if(currentOffHand == AngelTotem.ANGEL_TOTEM || currentMainHand == AngelTotem.ANGEL_TOTEM) {
                    if(respawnPosition != null) {
                        if(spawnPointHasBed) {
                            if(sameDimension) {
                                if(this.getAttacker() != null) {
                                    this.setAttacker(null);
                                    dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                                } else { 
                                    this.abilities.allowFlying = true; 
                                }
                            } else {
                                this.sendMessage(Text.of("You are not in the same dimension as your home bed."), true);
                                dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                            }
                        } else {
                            this.sendMessage(Text.of("Your home bed seems to be missing."), true);
                            dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                        }
                    } else {
                        this.sendMessage(Text.of("You must set a spawn point to use this totem"), true);
                        dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                    }
                } else {
                    this.abilities.allowFlying = false; 
                }
            }
        }
        this.sendAbilitiesUpdate();
    }   

    private void dropTotem(Item mainHandItem, Item offHandItem, PlayerInventory inventory, PlayerEntity player, World world) {
        if(player.getAbilities().flying) {
            if(mainHandItem == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
            }
            if(offHandItem == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 0.6f, 1.2f);
            }
        }
    }


}
