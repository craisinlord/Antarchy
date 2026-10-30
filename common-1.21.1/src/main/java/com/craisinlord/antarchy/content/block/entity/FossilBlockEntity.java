package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FossilBlockEntity extends BlockEntity {
    private static final String ENTITY_TAG = "Entity";

    @Nullable
    private ResourceLocation entityId;

    public FossilBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.FOSSIL_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Nullable
    public ResourceLocation entityId() {
        return this.entityId != null ? this.entityId : Fossils.entityAt(this.worldPosition);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        this.entityId = input.get(AntarchyObjects.FOSSIL_ENTITY_COMPONENT.get());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        ResourceLocation id = this.entityId();
        if (id != null) {
            builder.set(AntarchyObjects.FOSSIL_ENTITY_COMPONENT.get(), id);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(ENTITY_TAG);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.entityId = tag.contains(ENTITY_TAG) ? ResourceLocation.tryParse(tag.getString(ENTITY_TAG)) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.entityId != null) {
            tag.putString(ENTITY_TAG, this.entityId.toString());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
