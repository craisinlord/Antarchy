package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FossilBlockEntity extends BlockEntity {
    @Nullable
    private ResourceLocation entityId;

    public FossilBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.FOSSIL_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    public ResourceLocation entityId() {
        return this.entityId != null ? Fossils.resolve(this.entityId) : Fossils.entityAt(this.worldPosition);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.entityId = tag.contains(Fossils.ENTITY_TAG) ? ResourceLocation.tryParse(tag.getString(Fossils.ENTITY_TAG)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ResourceLocation id = this.entityId();
        if (id != null) {
            tag.putString(Fossils.ENTITY_TAG, id.toString());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
