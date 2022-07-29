package com.quattage.angelrings.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class AngelRingsMixin extends LivingEntity {
    public AngelRingsMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        
    }

}
