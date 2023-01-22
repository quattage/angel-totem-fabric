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
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class AngelTotemMixin extends LivingEntity {

    @Shadow
    @Final
    private PlayerAbilities abilities;

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @Shadow
    private final PlayerInventory inventory = new PlayerInventory((PlayerEntity)(Object)this);

    @Shadow
    public abstract void sendAbilitiesUpdate();

    protected AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    PlayerEntity currentPlayer = (PlayerEntity) (Object)this;
    World currentWorld = this.getWorld();

    boolean canUseTotem = false;

    boolean shouldDoTargetCheck = AngelTotem.getConfig().BasicTotemOptions.requireTarget;
    boolean isHardMode = AngelTotem.getConfig().BasicTotemOptions.hardMode;
    int maxConfigurableDistance = AngelTotem.getConfig().BasicTotemOptions.flightRadius;
    // boolean useReliefMode = AngelTotem.getConfig().BasicTotemOptions.reliefMode;

    NbtCompound totemNbt = new NbtCompound();
    // ServerPlayerEntity serverPlayer;


    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        currentWorld = currentPlayer.getWorld();
        if (!currentWorld.isClient() && !isCheating()) {
            if (isTotemEquipped()) {
                NbtCompound totemNbt = getTotemNbt();                
                String bindTargetKey = getTotemTargetKey(totemNbt);
                if (totemNbt.isEmpty()) {
                    // throw "totem invalid" error
                } else {
                    BlockPos bindPos = getTotemTargetPos(totemNbt);
                    boolean sameDimension = currentWorld.getRegistryKey().getValue().getPath().equals(totemNbt.getString("Dimension"));
                    if(stateToKey(getBlockStateAt(bindPos)).equals(bindTargetKey) && sameDimension) {

                        //////////// totem target is a bed, check if the player's spawn position matches the bed's location
                        if (bindTargetKey.contains("bed")) {
                            if(getRespawnPosition(currentPlayer).isWithinDistance(bindPos, 2d)) {
                                canUseTotem = true;
                            } else {
                                this.sendMessage(new TranslatableText("angeltotem.errorBedNotSpawnpoint", new TranslatableText(bindTargetKey)), true);  
                                canUseTotem = false;
                            }
                        
                        //////////// totem target is a respawn anchor, check if the player's spawn position matches the beacon and if the anchor has charges
                        } else if (bindTargetKey.equals("block.minecraft.respawn_anchor")) {
                            if(getRespawnPosition(currentPlayer).isWithinDistance(bindPos, 2d)) {
                                if(getAnchorCharges(totemNbt) > 0) {
                                    canUseTotem = true;
                                } else {
                                    this.sendMessage(new TranslatableText("angeltotem.errorAnchorOutOfCharges"), true); 
                                    canUseTotem = false;
                                }
                                
                            } else {
                                this.sendMessage(new TranslatableText("angeltotem.errorBedNotSpawnpoint", new TranslatableText(bindTargetKey)), true);   
                                canUseTotem = false;
                            }
                        
                        //////////// totem target is a beacon, check if the beacon is active and modify the flight distance 
                        } else if (bindTargetKey.equals("block.minecraft.beacon")) {
                            if(getBeaconLevels(totemNbt) > 0) {
                                canUseTotem = true;
                            } else {
                                this.sendMessage(new TranslatableText("angeltotem.errorBeaconInactive"), true);
                                canUseTotem = false;
                            }
                            
                        //////////// totem target is non-specific block, just assume they can fly
                        } else {
                            canUseTotem = true;
                        }
                    } else {
                        this.sendMessage(new TranslatableText("angeltotem.errorTargetNotFound", new TranslatableText(totemNbt.getString("BindingTarget"))), true); 
                        canUseTotem = false;
                    }

                    if (canUseTotem) {
                        this.abilities.allowFlying = true;
                        int distance = bindPos.getManhattanDistance(currentPlayer.getBlockPos());
                        drawIndicator(distance, maxConfigurableDistance, 30);
                        if(distance > maxConfigurableDistance) {
                            canUseTotem = false;
                            this.abilities.allowFlying = false;
                            dropTotem();
                            this.sendMessage(new TranslatableText("angeltotem.errorTargetOutOfRange", new TranslatableText(bindTargetKey)), true);
                        }
                    } else {
                        if(this.abilities.flying)
                            dropTotem();

                        this.abilities.allowFlying = false;
                    }
                }
            } else {
                this.abilities.allowFlying = false;
            }
        }
        this.sendAbilitiesUpdate();
    } 

    // Returns the levels of the beacon the totem is bound to
    int getBeaconLevels(NbtCompound totemNbtCompound) {
        BlockEntity totemTargetBlockEntity = getTotemTargetBlockEntity(totemNbtCompound);
        return totemTargetBlockEntity.toInitialChunkDataNbt().getInt("Levels");
    }

    // Returns the amount of charges of the respawn anchor the totem is bound to
    int getAnchorCharges(NbtCompound totemNbtCompound) {
        BlockState totemTargetState = getTotemTargetState(totemNbtCompound);
        return totemTargetState.get(RespawnAnchorBlock.CHARGES);
    }
    // additional implemtations for cross-mod compatability would go here

    //Returns the cooresponding TranslationKey for the given BlockState instance
    String stateToKey(BlockState state) {
        return state.getBlock().getTranslationKey();
    }

    //Returns the block's Translationkey as a string. Gets the block that the totem is bound to from the totem's NBT. 
    String getTotemTargetKey(NbtCompound totemNbtCompound) {
        if (totemNbtCompound.isEmpty())
            return null;
        return totemNbtCompound.getString("BindingTarget");
    }

    BlockState getBlockStateAt(BlockPos pos) {
        return currentWorld.getBlockState(pos);
    }

    // Returns BlockEntity found at the XYZ bind location of the totem. Useful for
    // reading NBT data of a block
    BlockEntity getTotemTargetBlockEntity(NbtCompound totemNbtCompound) {
        return currentWorld.getBlockEntity(getTotemTargetPos(totemNbtCompound));
    }

    // Returns BlockState found at the XYZ bind location of the totem
    BlockState getTotemTargetState(NbtCompound totemNbtCompound) {
        BlockState totemTargetState = currentWorld.getBlockState(getTotemTargetPos(totemNbtCompound));
        if (totemTargetState.isIn(AngelTotem.getValidTotemTargets()))
            return totemTargetState;
        return null;
    }

    // Returns BlockPos (X,Y,Z) of the block the totem is bound to, returns null if
    // the totem somehow has no NBT
    BlockPos getTotemTargetPos(NbtCompound totemNbtCompound) {
        if (!totemNbtCompound.isEmpty())
            return new BlockPos(totemNbtCompound.getDouble("PositionX"), totemNbtCompound.getDouble("PositionY"),
                    totemNbtCompound.getDouble("PositionZ"));
        return null;
    }

    // Returns NULL if the player has not changed their spawnpoint
    BlockPos getRespawnPosition(PlayerEntity player) {
        BlockPos position = ((ServerPlayerEntity) player).getSpawnPointPosition();
        if (position == null)
            return new BlockPos(0d, 0d, 0d);
        return position;
    }

    // Returns NBT of totems in inventory. There can be MORE THAN ONE totem in valid
    // slots (main hand, off hand, trinkets slot, etc.)
    // PRIORITIZES MAIN HAND IN CASES WHERE MULTIPLE TOTEMS ARE HELD SIMULTANEOUSLY
    NbtCompound getTotemNbt() {
        if (isMainHandTotemEquipped()) {
            return this.getMainHandStack().getNbt();
        }
        if (isOffHandTotemEquipped()) {
            return this.getOffHandStack().getNbt();
        }
        if (isTrinketTotemEquipped()) {
            return TrinketTotem.getTrinketNbt((PlayerEntity) (Object) this, currentWorld);
        }
        return new NbtCompound();
    }

    // Drops ALL held totems, as long as they're in slots that would enable flying
    // (main hand, off hand, trinkets slot, etc.)
    void dropTotem() {
        if (currentPlayer.getAbilities().flying) {
            if (isMainHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(inventory.selectedSlot).getNbt());
                inventory.removeStack(inventory.selectedSlot);
                currentPlayer.dropItem(totemToDrop, true);
                currentWorld.playSound(null, currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(),
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if (isOffHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getNbt());
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                currentPlayer.dropItem(totemToDrop, true);
                currentWorld.playSound(null, currentPlayer.getX(), currentPlayer.getY(), currentPlayer.getZ(),
                        SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if (isTrinketTotemEquipped()) {
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
        if (AngelTotem.getShouldUseTrinkets()) {
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
                    bar = bar.substring(0, barProgress) + "§f" + bar.substring(barProgress);
                }
                this.sendMessage(Text.of(bar), true);
            }
        }
    }

    boolean isCheating() {
        return this.abilities.creativeMode || ((PlayerEntity) (Object) this).isSpectator();
    }
}
