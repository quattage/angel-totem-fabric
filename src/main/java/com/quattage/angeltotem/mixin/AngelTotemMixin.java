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

    @Shadow
    public abstract void sendAbilitiesUpdate();

    @Shadow @Final
    private PlayerAbilities abilities;

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    public AngelTotemMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    PlayerEntity player = ((PlayerEntity) (Object) this);
    PlayerInventory inventory = player.getInventory();    
        
    World currentWorld;
    BlockPos totemBindLocation = null;
    BlockPos playerRespawnPosition = null;

    boolean spawnPointHasBed = false;
    boolean sameDimension = false;
    boolean canUseTotem = false;

    boolean doTargetCheck = AngelTotem.getConfig().BasicTotemOptions.requireTarget;
    int maxDistance = AngelTotem.getConfig().BasicTotemOptions.flightRadius;
    //boolean useReliefMode = AngelTotem.getConfig().BasicTotemOptions.reliefMode;
        
    NbtCompound totemNbt = null;
    //ServerPlayerEntity serverPlayer;






    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        currentWorld = player.getWorld();
        if(!currentWorld.isClient() && !isCheating()) {
            AngelTotem.messageLog("totem: " + isTotemEquipped());
            if(isTotemEquipped()) {
                this.abilities.allowFlying = true;
            } else {
                this.abilities.allowFlying = false;
            }
        }

        if(this.abilities.flying && !canUseTotem) {
            dropTotem();
        }

        this.sendAbilitiesUpdate();
    }

   

    //1 = BED,  2 = ANCHOR,  3 = BEACON,  0 = GENERIC
    int getBoundTargetType() {
        return 0;
    }

    //!!NOTE!! 
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

    void dropTotem() {
        if(player.getAbilities().flying) {
            if(isMainHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(inventory.selectedSlot).getNbt());
                inventory.removeStack(inventory.selectedSlot);
                player.dropItem(totemToDrop, true);
                currentWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(isOffHandTotemEquipped()) {
                ItemStack totemToDrop = new ItemStack(AngelTotem.BOUND_ANGEL_TOTEM, 1);
                totemToDrop.setNbt(inventory.getStack(PlayerInventory.OFF_HAND_SLOT).getNbt());
                inventory.removeStack(PlayerInventory.OFF_HAND_SLOT);
                player.dropItem(totemToDrop, true);
                currentWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                        SoundCategory.HOSTILE, 0.6f, 1.2f);
            }
            if(isTrinketTotemEquipped()) {
                TrinketTotem.dropTrinketTotem(player, world);
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
