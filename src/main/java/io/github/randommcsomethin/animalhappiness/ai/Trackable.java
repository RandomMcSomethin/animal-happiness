package io.github.randommcsomethin.animalhappiness.ai;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface Trackable {
    public static final TrackedData<Optional<BlockPos>> SEARCH_TARGET = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
}
