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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3i;
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
        int maximumAllowedDistance = AngelTotem.getConfig().AngelTotemOptions.bedFlightRadius;
        boolean useReliefMode = AngelTotem.getConfig().AngelTotemOptions.reliefMode;

        if(!world.isClient()) {
                serverPlayer = (ServerPlayerEntity) (Object) this;
                respawnPosition = serverPlayer.getSpawnPointPosition();
                
                if(respawnPosition != null)
                    spawnPointHasBed = currentWorld.getBlockState(respawnPosition).getBlock() == Blocks.RED_BED;
                else
                    spawnPointHasBed = false;
                sameDimension = serverPlayer.getSpawnPointDimension() == currentWorld.getRegistryKey();
        
            //there's no point in doing any of this if the player is in creative
            if(!this.abilities.creativeMode) {
                //if the player is holding the totem
                if(currentOffHand == AngelTotem.ANGEL_TOTEM || currentMainHand == AngelTotem.ANGEL_TOTEM) {         
                    //if the player is in the same dimension as their respawn position                
                    if(!sameDimension) {    
                        this.sendMessage(new TranslatableText("angeltotem.errorDimensionMismatch"), true);
                        canUseTotem = false;             
                    } else {
                        //if the player even has a valid respawn position (this will rarely happen but yknow)
                        if(respawnPosition == null) {                                                                                      
                            this.sendMessage(new TranslatableText("angeltotem.errorBedNotFound"), true);                 
                            canUseTotem = false;                                                                                 
                        } else {                                                                                                  
                            if(!spawnPointHasBed) {                                
                                //if there is a bed at the player's spawn location                                                     
                                this.sendMessage(new TranslatableText("angeltotem.errorBedMissing"), true);  
                                canUseTotem = false;                                                                           
                            } else {            
                                //assign an int to keep track of distance between player and bed            
                                int blockPosDistance = respawnPosition.getManhattanDistance(new Vec3i((int) Math.round(this.getX()), (int) Math.round(this.getY()), (int) Math.round(this.getZ())));
                                //assign a float to calculate percent of configured distance the player currently is
                                float distPercent = (AngelTotem.clampValue((float) blockPosDistance / (float) maximumAllowedDistance, 0f, 1f));
                                //the width of the 
                                int barWidth = AngelTotem.getConfig().AngelTotemOptions.indicatorWidth;
                                String bar = "§a";
                                if(distPercent > 0.5f)
                                    bar = "§6";
                                if(distPercent > 0.8f)
                                    bar = "§4";
                                if(blockPosDistance < maximumAllowedDistance + 4) {
                                    if(barWidth > 0) {
                                        if(barWidth < 15)
                                            barWidth = 15;
                                        for(int pipe = 0; pipe < barWidth; pipe++) {
                                            bar += "|";
                                        }

                                        if(distPercent > 0.1 && distPercent < 0.99) {
                                            int barProgress = (int) (bar.length() * distPercent);
                                            bar = bar.substring (0, barProgress) + "§f" + bar.substring(barProgress);
                                        }
                                        this.sendMessage(Text.of(bar), true);
                                    }
                                    canUseTotem = true;       
                                } else {
                                    if(canUseTotem) {
                                        canUseTotem = false;
                                    } else {
                                        this.sendMessage(new TranslatableText("angeltotem.errorBedOutOfRange"), true);
                                    }
                                }                                                                                                        
                            }
                        }
                    }
                    //if the totem is disabled, but the player is flying, remove the player's ability to fly and drop the totem.
                    if(canUseTotem == false && this.abilities.flying) {
                        dropTotem(currentMainHand, currentOffHand, activeInventory, (PlayerEntity) (Object) this, world);
                        this.abilities.allowFlying = false;
                    }
                } else {
                    //if the player is not using the totem, disable it and remove the flying effect
                    canUseTotem = false;
                    this.abilities.allowFlying = false;
                }
                
                //if the previous code has determined that the player can use the totem, then give them the ability to fly
                if(canUseTotem) 
                    this.abilities.allowFlying = true;
                else
                    this.abilities.allowFlying = false;
            } else {
                if(this.abilities.creativeMode || this.isSpectator()) {
                    this.abilities.allowFlying = true;
                }
            }
        }
        this.sendAbilitiesUpdate();
    }   

    void dropTotem(Item mainHandItem, Item offHandItem, PlayerInventory inventory, PlayerEntity player, World world) {
        if(player.getAbilities().flying) {
            if(mainHandItem == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(offHandItem == AngelTotem.ANGEL_TOTEM) {
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(AngelTotem.ANGEL_TOTEM);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
        }
    }
}
