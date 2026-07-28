package com.craisinlord.antarchy.content.block.entity;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BluestoneComparatorBlockEntity extends BlockEntity {
    private int outputSignal;

    public BluestoneComparatorBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<BluestoneComparatorBlockEntity>> blockEntityTypeSupplier) {
        super(blockEntityTypeSupplier.get(), pos, state);
    }

    public int getOutputSignal() {
        return this.outputSignal;
    }

    public void setOutputSignal(int outputSignal) {
        this.outputSignal = outputSignal;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("OutputSignal", this.outputSignal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.outputSignal = tag.getInt("OutputSignal");
    }
}
