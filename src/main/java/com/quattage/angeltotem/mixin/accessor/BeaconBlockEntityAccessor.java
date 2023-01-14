package com.quattage.angeltotem.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.entity.BeaconBlockEntity;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
	@Accessor("beamSections")
	List<BeaconBlockEntity.BeamSegment> beamSections();

	@Accessor("levels")
	int getLevels();
}