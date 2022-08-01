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
        boolean canUseTotem = false;
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

                //if the totem is disabled, but the player is flying, remove the player's ability to fly and drop the totem, if they're holding it.
                if(!canUseTotem && this.abilities.flying) {
                    this.abilities.allowFlying = false;
                    dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                }

                //if the player is holding a totem, check if they have a respawn position, if they do, check if there is a bed at their respawn position, if they do, check if they are in the same dimension as their bed.
                if(currentOffHand == AngelTotem.ANGEL_TOTEM || currentMainHand == AngelTotem.ANGEL_TOTEM) {                         
                    if(!sameDimension) {    
                        this.sendMessage(Text.of("You are not in the same dimension as your home bed."), true);
                        canUseTotem = false;      
                        this.abilities.allowFlying = false;        
                    } else {
                        if(respawnPosition == null) {                                                                                      
                            this.sendMessage(Text.of("You must have a home bed to use this Totem"), true);                       
                            canUseTotem = false;   
                            this.abilities.allowFlying = false;                                                                                 
                        } else {                                                                                                  
                            if(!spawnPointHasBed) {                                                                                     
                                this.sendMessage(Text.of("Your home bed appears to be missing." ), true);  
                                canUseTotem = false;                          
                                this.abilities.allowFlying = false;                                                      
                            } else {                                                                                                
                                canUseTotem = true;                                                                                                               
                            }
                        }
                    }
                    if(canUseTotem)
                        this.abilities.allowFlying = true;
                    else   
                        this.abilities.allowFlying = false;
                }
                //check if the player is attacked. if they are, disable the totem. if they aren't re-enable the totem.
                if(this.getAttacker() != null) {
                    this.setAttacker(null); 
                    canUseTotem = false;
                } else {
                    canUseTotem = true;
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
