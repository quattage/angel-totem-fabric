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

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BlockEntity;
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

    @Shadow
    public abstract void sendAbilitiesUpdate();

    @Shadow @Final
    private PlayerAbilities abilities;

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    public AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    PlayerEntity currentPlayer = ((PlayerEntity) (Object) this);
    PlayerInventory inventory = currentPlayer.getInventory();    
        
    World currentWorld;
    BlockPos totemBindLocation = null;
    BlockPos playerRespawnPosition = null;

    boolean spawnPointHasBed = false;
    boolean sameDimension = false;
    boolean canUseTotem = false;

    boolean shouldDoTargetCheck = AngelTotem.getConfig().BasicTotemOptions.requireTarget;
    boolean isHardMode  = AngelTotem.getConfig().BasicTotemOptions.hardMode;
    int maxDistance = AngelTotem.getConfig().BasicTotemOptions.flightRadius;
    //boolean useReliefMode = AngelTotem.getConfig().BasicTotemOptions.reliefMode;
        
    NbtCompound totemNbt = null;
    //ServerPlayerEntity serverPlayer;


    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        currentWorld = currentPlayer.getWorld();
        if(!currentWorld.isClient() && !isCheating()) {
            if(isTotemEquipped()) {
                NbtCompound totemNbt = getTotemNbt(); 
                String boundTarget = getTargetType(totemNbt);
                
                if(totemNbt.isEmpty()) {
                    //throw "invalid totem/something went wrong" error

                ////////////////////////////////////////////////////  BED  ////////////////////////////////////////////
                } else if(boundTarget.equals("BED")) {
                    AngelTotem.messageLog("BOUND TO BED");
                    BlockPos totemBind = getTotemTargetLocation(totemNbt);
                    if(getWorldTargetType(totemBind).equals("BED")) {
                        if(!getRespawnTargetLocation(currentPlayer).isWithinDistance(totemBind, 2d)) {
                            //throw "bed not spawnpoint" error
                            canUseTotem = false;
                        } else 
                            canUseTotem = true;
                    } else {
                        //throw "totem target does not exist" error
                        canUseTotem = false;
                    }   
                //////////////////////////////////////////////////// ANCHOR ////////////////////////////////////////////
                } else if(boundTarget.equals("ANCHOR")) {
                    AngelTotem.messageLog("BOUND TO ANCHOR");
                    BlockPos totemBind = getTotemTargetLocation(totemNbt);
                    if(getWorldTargetType(totemBind).equals("ANCHOR")) {
                        if(!getRespawnTargetLocation(currentPlayer).isWithinDistance(totemBind, 2d)) {
                            //throw "bed not spawnpoint" error
                            canUseTotem = false;
                        } else {
                            if(getAnchorCharges(totemNbt) <=0) {
                                //throw "anchor out of charges" error
                                canUseTotem = false; 
                            } else
                                canUseTotem = true;
                        }
                    } else {
                        //throw "totem target does not exist" error
                        canUseTotem = false;
                    }
                //////////////////////////////////////////////////// BEACON ////////////////////////////////////////////
                } else if (boundTarget.equals("BEACON")) {
                    AngelTotem.messageLog("BOUND TO BEACON");
                //if bound to beacon
                    //if beacon exists at spawnpoint 
                        //if beacon levels > 0;
                            //modify flight distance by beacon multiplier
                        //
                        //ELSE
                            //throw "beacon not activated" erorr
                            //canUseTotem = false;
                    //
                    //ELSE
                        //throw "beacon missing" error
                        //canUseTotem = false;
                
                
            } else {
                this.abilities.allowFlying = false;
                canUseTotem = false;
            }

            if(canUseTotem) {
                //draw indicator
                //allow player to fly
                //do range check with maximum distance relative to their totem target
                //if they exceed the range, disable canUseTotem
                //woo done

            } else if(this.abilities.flying)
                dropTotem();
        }
        this.sendAbilitiesUpdate();
    }

    //Returns the levels of the beacon the totem is bound to
    int getBeaconLevels(NbtCompound totemNbtCompound) {
        BlockEntity totemTargetBlockEntity = getTotemTargetBlockEntity(totemNbtCompound);
        return totemTargetBlockEntity.toInitialChunkDataNbt().getInt("Levels");
    }

    //Returns the amount of charges of the respawn anchor the totem is bound to
    int getAnchorCharges(NbtCompound totemNbtCompound) {
        BlockState totemTargetState = getTotemTargetState(totemNbtCompound);
        return totemTargetState.get(RespawnAnchorBlock.CHARGES);
    }
    //additional implemtations for cross-mod compatability would go here

    //Returns "BED" , "ANCHOR" , or "BEACON" , falls back on "GENERIC" if it finds any other block in the targets list,
    //or "INVALID" if it doesn't find a relevent block (should never happen) Also returns "null" if the totem somehow has
    //no NBT (should also never happen)
    String getTargetType(NbtCompound totemNbtCompound) {
        if(totemNbtCompound.isEmpty())
            return null;
        BlockState totemTargetState = getTotemTargetState(totemNbtCompound);
        String totemTargetKey = totemTargetState.getBlock().getTranslationKey().toUpperCase();
        if(totemTargetKey.contains("BED")) 
            return "BED";
        if(totemTargetState.getBlock().equals(Blocks.RESPAWN_ANCHOR))
            return "ANCHOR";
        if(totemTargetState.getBlock().equals(Blocks.BEACON)) 
            return "BEACON";
        if(totemTargetState.isIn(AngelTotem.getValidTotemTargets()))
            return "GENERIC";
        return "INVALID";
        //additional implentations for cross-mod compatability would go here
    }

    String getWorldTargetType(BlockPos pos) {
        BlockState totemTargetState = currentWorld.getBlockState(pos);
        String totemTargetKey = totemTargetState.getBlock().getTranslationKey().toUpperCase();
        if(totemTargetKey.contains("BED")) 
            return "BED";
        if(totemTargetState.getBlock().equals(Blocks.RESPAWN_ANCHOR))
            return "ANCHOR";
        if(totemTargetState.getBlock().equals(Blocks.BEACON)) 
            return "BEACON";
        if(totemTargetState.isIn(AngelTotem.getValidTotemTargets()))
            return "GENERIC";
        return "INVALID";
        //additional implentations for cross-mod compatability would go here
    }

    BlockState getBlockStateAt(BlockPos pos) {
        return currentWorld.getBlockState(pos);
    }

    //Returns BlockEntity found at the XYZ bind location of the totem. Useful for reading NBT data of a block
    BlockEntity getTotemTargetBlockEntity(NbtCompound totemNbtCompound) {
        return currentWorld.getBlockEntity(getTotemTargetLocation(totemNbtCompound));
    }

    
    //Returns BlockState found at the XYZ bind location of the totem
    BlockState getTotemTargetState(NbtCompound totemNbtCompound) {
        return currentWorld.getBlockState(getTotemTargetLocation(totemNbtCompound));
    }

    //Returns BlockPos (X,Y,Z) of the block the totem is bound to, returns null if the totem somehow has no NBT
    BlockPos getTotemTargetLocation(NbtCompound totemNbtCompound) {
        if(!totemNbtCompound.isEmpty())
            return new BlockPos(totemNbtCompound.getDouble("PositionX"), totemNbtCompound.getDouble("PositionY"), totemNbtCompound.getDouble("PositionZ"));
        return null;
    } 

    // Returns NULL if the player has not changed their spawnpoint
    BlockPos getRespawnTargetLocation(PlayerEntity player) {
        return ((ServerPlayerEntity)player).getSpawnPointPosition();
    }

    //Returns NBT of totems in inventory. There can be MORE THAN ONE totem in valid slots (main hand, off hand, trinkets slot, etc.)
    //PRIORITIZES MAIN HAND IN CASES WHERE MULTIPLE TOTEMS ARE HELD SIMULTANEOUSLY
    NbtCompound getTotemNbt() {
        if(isMainHandTotemEquipped()) {
            return this.getMainHandStack().getNbt();
        }
        if(isOffHandTotemEquipped()) {
            return this.getOffHandStack().getNbt();
        }
        if(isTrinketTotemEquipped()) {
            return TrinketTotem.getTrinketNbt((PlayerEntity) (Object) this, currentWorld);
        }
        return new NbtCompound();
    }

    //Drops ALL held totems, as long as they're in slots that would enable flying (main hand, off hand, trinkets slot, etc.)
    void dropTotem() {
        if(currentPlayer.getAbilities().flying) {
            if(isMainHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(inventory.selectedSlot).getNbt());
                inventory.removeStack(inventory.selectedSlot);
                currentPlayer.dropItem(totemToDrop, true);
                currentWorld.playSound(null, currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(isOffHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getNbt());
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                currentPlayer.dropItem(totemToDrop, true);
                currentWorld.playSound(null, currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(isTrinketTotemEquipped()) {
                TrinketTotem.dropTrinketTotem(currentPlayer, world);
            }
        }
    }

    ////
    boolean isTotemEquipped() {
        return isOffHandTotemEquipped() || isMainHandTotemEquipped() || isTrinketTotemEquipped();
    }

    ////
    boolean isOffHandTotemEquipped() {
        return this.getOffHandStack().getItem() == AngelTotem.BOUND_ANGEL_TOTEM;
    }

    ////
    boolean isMainHandTotemEquipped() {
        return this.getMainHandStack().getItem() == AngelTotem.BOUND_ANGEL_TOTEM;
    }

    ////
    boolean isTrinketTotemEquipped() {
        if(AngelTotem.getShouldUseTrinkets()) {
            return TrinketTotem.isTrinketEquipped;
        }        
        return false;
    }


    void drawIndicator(int currentValue, int maximumValue, int barWidth) {
        float progressPercent = MathHelper.clampValue((float) currentValue / (float) maximumValue, 0f, 1f);
        String bar = "§a";
        if (progressPercent > 0.5f)
            bar = "§6";
        if (progressPercent > 0.8f)
            bar = "§4";
        if (currentValue < maximumValue + 4) {
            if (barWidth > 0) {
                if (barWidth < 15)
                    barWidth = 15;
                    for (int pipe = 0; pipe < barWidth; pipe++) {
                        bar += "|";
                    }
                    if (progressPercent > 0.1 && progressPercent < 0.99) {
                        int barProgress = (int) (bar.length() * progressPercent);
                        bar = bar.substring(0, barProgress) + "§f"+ bar.substring(barProgress);
                    }
                this.sendMessage(Text.of(bar), true);
            }
        }
    }

    boolean isCheating() {
        return this.abilities.creativeMode || ((PlayerEntity) (Object) this).isSpectator();
    }
}
