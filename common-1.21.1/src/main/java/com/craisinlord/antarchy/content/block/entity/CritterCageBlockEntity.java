package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.CritterCageItem;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CritterCageBlockEntity extends BlockEntity {
    private static final String ENTITY_TYPE_TAG = "EntityType";
    private static final String ENTITY_DATA_TAG = "EntityData";
    private static final String PRIMARY_COLOR_TAG = "PrimaryColor";
    private static final String SECONDARY_COLOR_TAG = "SecondaryColor";
    private static final String CUSTOM_NAME_TAG = "CustomName";

    private ResourceLocation entityTypeId;
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
    public ResourceLocation entityTypeId() {
        return this.entityTypeId;
    }

    @Nullable
    public CompoundTag entityData() {
        return this.entityData == null ? null : this.entityData.copy();
    }

    @Nullable
    public Entity getOrCreateClientDisplayEntity(Level level) {
        if (level == null || level.isClientSide == false) {
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

        Entity entity = EntityType.loadEntityRecursive(savedData, level, loaded -> loaded);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.entityTypeId != null) {
            tag.putString(ENTITY_TYPE_TAG, this.entityTypeId.toString());
        }
        if (this.entityData != null) {
            tag.put(ENTITY_DATA_TAG, this.entityData.copy());
        }
        tag.putInt(PRIMARY_COLOR_TAG, this.primaryColor);
        tag.putInt(SECONDARY_COLOR_TAG, this.secondaryColor);
        if (this.customName != null) {
            tag.putString(CUSTOM_NAME_TAG, this.customName.getString());
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.entityTypeId = tag.contains(ENTITY_TYPE_TAG, Tag.TAG_STRING) ? ResourceLocation.parse(tag.getString(ENTITY_TYPE_TAG)) : null;
        this.entityData = tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND) ? tag.getCompound(ENTITY_DATA_TAG).copy() : null;
        this.primaryColor = tag.getInt(PRIMARY_COLOR_TAG);
        this.secondaryColor = tag.getInt(SECONDARY_COLOR_TAG);
        this.customName = tag.contains(CUSTOM_NAME_TAG, Tag.TAG_STRING) ? Component.literal(tag.getString(CUSTOM_NAME_TAG)) : null;
        this.clientDisplayEntity = null;
        this.dataVersion++;
    }

    private CompoundTag extractEntityData(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(ENTITY_DATA_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(ENTITY_DATA_TAG).copy();
    }
}
