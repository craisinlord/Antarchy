package com.craisinlord.antarchy.content.block.entity;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("OutputSignal", this.outputSignal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.outputSignal = input.getIntOr("OutputSignal", 0);
    }
}
