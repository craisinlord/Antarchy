package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.placerUuid = input.read("Placer", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("Placer", UUIDUtil.CODEC, this.placerUuid);
    }
}
