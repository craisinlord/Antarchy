package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DreamCampfireBlockEntity extends CampfireBlockEntity {
    private static final ThreadLocal<Boolean> CONSTRUCTING_DREAM_CAMPFIRE = ThreadLocal.withInitial(() -> false);

    public DreamCampfireBlockEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    public BlockEntityType<?> getType() {
        return AntarchyObjects.DREAM_CAMPFIRE_BLOCK_ENTITY.get();
    }

    public static void beginConstruction() {
        CONSTRUCTING_DREAM_CAMPFIRE.set(true);
    }

    public static void endConstruction() {
        CONSTRUCTING_DREAM_CAMPFIRE.set(false);
    }

    public static boolean isConstructingDreamCampfire() {
        return CONSTRUCTING_DREAM_CAMPFIRE.get();
    }
}
