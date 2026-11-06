package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class FossilBlockEntity extends BlockEntity {
    @Nullable
    private Identifier entityId;

    public FossilBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.FOSSIL_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    public Identifier entityId() {
        return this.entityId != null ? Fossils.resolve(this.entityId) : Fossils.entityAt(this.worldPosition);
    }

    public void setEntityId(@Nullable Identifier entityId) {
        this.entityId = entityId;
        this.setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.entityId = input.read("Entity", Identifier.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("Entity", Identifier.CODEC, this.entityId);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}