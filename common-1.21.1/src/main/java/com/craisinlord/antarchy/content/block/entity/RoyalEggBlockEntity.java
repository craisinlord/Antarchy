package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RoyalEggBlockEntity extends BlockEntity {
    @Nullable
    private UUID placerUuid;

    public RoyalEggBlockEntity(BlockPos pos, BlockState state) {
        super(AntarchyObjects.ROYAL_EGG_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public UUID getPlacerUuid() {
        return this.placerUuid;
    }

    public void setPlacerUuid(@Nullable UUID placerUuid) {
        this.placerUuid = placerUuid;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.placerUuid = tag.hasUUID("Placer") ? tag.getUUID("Placer") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.placerUuid != null) {
            tag.putUUID("Placer", this.placerUuid);
        }
    }
}
