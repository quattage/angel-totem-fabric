package com.quattage.angeltotem.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.quattage.angeltotem.AngelTotem;
import com.quattage.angeltotem.compat.TrinketTotem;
import com.quattage.angeltotem.helpers.MathHelper;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//@SuppressWarnings("unused")
@Mixin(PlayerEntity.class)
public abstract class AngelTotemMixin extends LivingEntity {

    @Shadow public abstract void sendAbilitiesUpdate();
    @Shadow @Final private PlayerAbilities abilities;
    @Shadow public abstract void sendMessage(Text message, boolean actionBar);
    private boolean trinketEquip;

    public AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        ItemStack currentOffHand = this.getOffHandStack();
        ItemStack currentMainHand = this.getMainHandStack();
        PlayerInventory activeInventory = ((PlayerEntity) (Object) this).getInventory();
        BlockPos respawnPosition = null;
        boolean spawnPointHasBed = false;
        boolean sameDimension = false;
        boolean canUseTotem = false;
        World currentWorld = this.getWorld();
        int maximumAllowedDistance = AngelTotem.getConfig().BasicTotemOptions.bedFlightRadius;
        boolean doBedCheck = AngelTotem.getConfig().BasicTotemOptions.doBedCheck;
        NbtCompound totemNbt = null;
        ServerPlayerEntity serverPlayer;
        //boolean useReliefMode = AngelTotem.getConfig().BasicTotemOptions.reliefMode;
        
        if(AngelTotem.getShouldUseTrinkets()) 
            trinketEquip = TrinketTotem.isTrinketEquipped;
        else 
            trinketEquip = false;
            
        
        if(!world.isClient()) {
            //there's no point in doing any of this if the player is in creative
            if(!this.abilities.creativeMode) {
                //if the player is holding the totem
                
                if(currentOffHand.getItem() == AngelTotem.BOUND_ANGEL_TOTEM || currentMainHand.getItem() == AngelTotem.BOUND_ANGEL_TOTEM || trinketEquip) {   
                    if(currentOffHand.getItem() == AngelTotem.BOUND_ANGEL_TOTEM) {
                        totemNbt = currentOffHand.getNbt();
                    }
                    if(currentMainHand.getItem() == AngelTotem.BOUND_ANGEL_TOTEM) {
                        totemNbt = currentMainHand.getNbt();
                    }
                    if(trinketEquip) {
                        totemNbt = TrinketTotem.getTrinketNbt((PlayerEntity) (Object) this, world);
                    }
                    if(totemNbt != null) {
                        if(totemNbt.contains("PositionX") && totemNbt.contains("PositionY") && totemNbt.contains("PositionZ")) {
                            respawnPosition = new BlockPos(totemNbt.getDouble("PositionX"), totemNbt.getDouble("PositionY"), totemNbt.getDouble("PositionZ"));
                        }
                    }
                    //fix this soon
                    sameDimension = world.getRegistryKey().getValue().getPath().equals(totemNbt.getString("Dimension"));
                    if(doBedCheck) {             
                        if(!sameDimension) {    
                            this.sendMessage(new TranslatableText("angeltotem.errorDimensionMismatch", new TranslatableText(totemNbt.getString("BindingTarget"))), true);
                            canUseTotem = false;             
                        } else {
                            //if the player even has a valid respawn position (this will rarely happen but yknow)
                            if(respawnPosition == null) {                                                                                      
                                this.sendMessage(new TranslatableText("angeltotem.errorTotemUnbound"), true);                 
                                canUseTotem = false;                                                                                 
                            } else {                              
                                spawnPointHasBed = currentWorld.getBlockState(respawnPosition).isIn(AngelTotem.getValidTotemTargets());                                                                    
                                if(!spawnPointHasBed) {                                                                               
                                    this.sendMessage(new TranslatableText("angeltotem.errorTargetMissing", new TranslatableText(totemNbt.getString("BindingTarget"))), true);  
                                    canUseTotem = false;                                                                           
                                } else {            
                                    serverPlayer = (ServerPlayerEntity) (Object) this;
                                    if((totemNbt.getString("BindingTarget").toUpperCase().contains("ANCHOR") || totemNbt.getString("BindingTarget").toUpperCase().contains("BED")) && (!respawnPosition.isWithinDistance(serverPlayer.getSpawnPointPosition(), 6d))) {
                                        this.sendMessage(new TranslatableText("angeltotem.errorBedNotSpawnpoint", new TranslatableText(totemNbt.getString("BindingTarget"))), true);   
                                    } else {
                                        BlockState respawnBlock = world.getBlockState(respawnPosition);
                                        if(respawnBlock.getBlock() == Blocks.RESPAWN_ANCHOR && respawnBlock.get(RespawnAnchorBlock.CHARGES) == 0) {
                                            this.sendMessage(new TranslatableText("angeltotem.errorAnchorOutOfCharges"), true);   
                                        } else {
                                            if(respawnBlock.getBlock() == Blocks.BEACON) {
                                                int beaconLevels = currentWorld.getBlockEntity(respawnPosition).toInitialChunkDataNbt().getInt("Levels");
                                                if(beaconLevels == 0) {
                                                    this.sendMessage(new TranslatableText("angeltotem.errorBeaconInactive"), true);
                                                    canUseTotem = false;
                                                } else {
                                                    //AngelTotem.messageLog("BEACON LEVELS: " + beaconLevels);
                                                    if(beaconLevels == 4) {
                                                        maximumAllowedDistance *= 3;
                                                    } else if(beaconLevels == 3) {
                                                        maximumAllowedDistance *= 2;
                                                    } else if(beaconLevels == 2) {
                                                        maximumAllowedDistance *= 1.5;
                                                    } else {
                                                        maximumAllowedDistance *= 1;
                                                    }
                                                }
                                            }
                                            //assign an int to keep track of distance between player and bed            
                                            int blockPosDistance = respawnPosition.getManhattanDistance(new Vec3i((int) Math.round(this.getX()), (int) Math.round(this.getY()), (int) Math.round(this.getZ())));
                                            //assign a float to calculate percent of configured distance the player currently is
                                            float distPercent = (MathHelper.clampValue((float) blockPosDistance / (float) maximumAllowedDistance, 0f, 1f));
                                            //the width of the 
                                            int barWidth = AngelTotem.getConfig().AdvancedTotemOptions.indicatorWidth;
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
                                                    this.sendMessage(new TranslatableText("angeltotem.errorTargetOutOfRange", new TranslatableText(totemNbt.getString("BindingTarget"))), true);
                                                }
                                            }     
                                        }
                                    }                                                                                                   
                                }
                            }
                        }
                    }
                    // if the totem is disabled, but the player is flying, remove the player's ability to fly and drop the totem.
                    if(canUseTotem == false && this.abilities.flying) {
                        dropTotem(currentMainHand.getItem(), currentOffHand.getItem(), activeInventory, (PlayerEntity) (Object) this, world);
                        this.abilities.allowFlying = false;
                    }
                } else {
                    // if the player is not using the totem, disable it and remove the flying effect
                    canUseTotem = false;
                    this.abilities.allowFlying = false;
                }
                
                //if the previous code has determined that the player can use the totem, then give them the ability to fly
                if(canUseTotem) 
                    this.abilities.allowFlying = true;
                else
                    this.abilities.allowFlying = false;
            } else {
                if(this.abilities.creativeMode) {
                    this.abilities.allowFlying = true;
                }
                if(((PlayerEntity) (Object) this).isSpectator()) {
                    this.noClip = true;
                }
            }
        }
        this.sendAbilitiesUpdate();
    }   

    void dropTotem(Item mainHandItem, Item offHandItem, PlayerInventory inventory, PlayerEntity player, World world) {
        if(player.getAbilities().flying) {
            if(mainHandItem == AngelTotem.BOUND_ANGEL_TOTEM) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(inventory.selectedSlot).getNbt());
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(totemToDrop, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(offHandItem == AngelTotem.BOUND_ANGEL_TOTEM) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getNbt());
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(totemToDrop, true);
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(trinketEquip) 
                TrinketTotem.dropTrinketTotem(player, world);
        }
    }
}
