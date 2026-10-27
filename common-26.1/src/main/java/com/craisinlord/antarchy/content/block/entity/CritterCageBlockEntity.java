package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.CritterCageItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CritterCageBlockEntity extends BlockEntity {
    private static final String ENTITY_TYPE_TAG = "EntityType";
    private static final String ENTITY_DATA_TAG = "EntityData";
    private static final String PRIMARY_COLOR_TAG = "PrimaryColor";
    private static final String SECONDARY_COLOR_TAG = "SecondaryColor";
    private static final String CUSTOM_NAME_TAG = "CustomName";

    private Identifier entityTypeId;
    private CompoundTag entityData;
    private int primaryColor = -1;
    private int secondaryColor = -1;
    @Nullable
    private Component customName;
    private int dataVersion;

    private transient int clientVersion = -1;
    @Nullable
    private transient Entity clientDisplayEntity;

    public CritterCageBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.CRITTER_CAGE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void loadFromItem(ItemStack stack, HolderLookup.Provider registries) {
        this.entityTypeId = stack.get(AntarchyObjects.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.get());
        this.entityData = this.extractEntityData(stack);
        this.primaryColor = stack.getOrDefault(AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.get(), -1);
        this.secondaryColor = stack.getOrDefault(AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.get(), -1);
        this.customName = stack.has(DataComponents.CUSTOM_NAME) ? stack.get(DataComponents.CUSTOM_NAME) : null;
        this.dataVersion++;
        this.clientDisplayEntity = null;
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(AntarchyObjects.CRITTER_CAGE.get());
        if (this.entityTypeId != null) {
            stack.set(AntarchyObjects.CRITTER_CAGE_ENTITY_TYPE_COMPONENT.get(), this.entityTypeId);
        }
        if (this.entityData != null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, customData -> customData.put(ENTITY_DATA_TAG, this.entityData.copy()));
        }
        if (this.primaryColor >= 0) {
            stack.set(AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.get(), this.primaryColor);
        } else {
            stack.remove(AntarchyObjects.CRITTER_CAGE_PRIMARY_COLOR_COMPONENT.get());
        }
        if (this.secondaryColor >= 0) {
            stack.set(AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.get(), this.secondaryColor);
        } else {
            stack.remove(AntarchyObjects.CRITTER_CAGE_SECONDARY_COLOR_COMPONENT.get());
        }
        if (this.customName != null) {
            stack.set(DataComponents.CUSTOM_NAME, this.customName);
        }
        return stack;
    }

    public int primaryColor() {
        return this.primaryColor;
    }

    public int secondaryColor() {
        return this.secondaryColor;
    }

    @Nullable
    public Identifier entityTypeId() {
        return this.entityTypeId;
    }

    @Nullable
    public CompoundTag entityData() {
        return this.entityData == null ? null : this.entityData.copy();
    }

    @Nullable
    public Entity getOrCreateClientDisplayEntity(Level level) {
        if (level == null || level.isClientSide() == false) {
            return null;
        }
        if (this.clientDisplayEntity != null && this.clientVersion == this.dataVersion) {
            return this.clientDisplayEntity;
        }

        this.clientVersion = this.dataVersion;
        this.clientDisplayEntity = this.createDisplayEntity(level);
        return this.clientDisplayEntity;
    }

    @Nullable
    private Entity createDisplayEntity(Level level) {
        if (this.entityTypeId == null || this.entityData == null) {
            return null;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(this.entityTypeId).orElse(null);
        if (type == null) {
            return null;
        }

        CompoundTag savedData = this.entityData.copy();
        savedData.putString("id", this.entityTypeId.toString());

        Entity entity = EntityType.loadEntityRecursive(savedData, level, EntitySpawnReason.LOAD, loaded -> loaded);
        if (entity == null) {
            return null;
        }

        entity.setUUID(UUID.randomUUID());
        entity.setNoGravity(true);
        entity.noPhysics = true;
        entity.setSilent(true);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        if (this.customName != null) {
            entity.setCustomName(this.customName);
        }
        entity.setCustomNameVisible(false);
        return entity;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.entityTypeId != null) {
            output.putString(ENTITY_TYPE_TAG, this.entityTypeId.toString());
        }
        if (this.entityData != null) {
            output.store(ENTITY_DATA_TAG, CompoundTag.CODEC, this.entityData.copy());
        }
        output.putInt(PRIMARY_COLOR_TAG, this.primaryColor);
        output.putInt(SECONDARY_COLOR_TAG, this.secondaryColor);
        if (this.customName != null) {
            output.putString(CUSTOM_NAME_TAG, this.customName.getString());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.entityTypeId = input.getString(ENTITY_TYPE_TAG).map(Identifier::parse).orElse(null);
        this.entityData = input.read(ENTITY_DATA_TAG, CompoundTag.CODEC).map(CompoundTag::copy).orElse(null);
        this.primaryColor = input.getIntOr(PRIMARY_COLOR_TAG, 0);
        this.secondaryColor = input.getIntOr(SECONDARY_COLOR_TAG, 0);
        this.customName = input.getString(CUSTOM_NAME_TAG).map(Component::literal).orElse(null);
        this.clientDisplayEntity = null;
        this.dataVersion++;
    }

    private CompoundTag extractEntityData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        return tag.getCompound(ENTITY_DATA_TAG).map(CompoundTag::copy).orElse(null);
    }
}
