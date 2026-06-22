package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.WaspNestBlock;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WaspNestBlockEntity extends BlockEntity {
    private static final String STORED_WASPS = "StoredWasps";
    private static final String PANDA_VARIANT = "PandaVariant";
    private static final int PANDA_VARIANT_CHANCE = 20;
    private final List<StoredWasp> storedWasps = new ArrayList<>();
    private int releaseCooldown = 100;
    private boolean initialized = false;
    private boolean pandaVariant;

    public WaspNestBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.WASP_NEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, WaspNestBlockEntity nest) {
        if (!nest.initialized) {
            nest.populateInitialWasps(level);
        }

        if (level.isNight() || level.isRaining()) {
            return;
        }

        if (nest.storedWasps.isEmpty()) {
            return;
        }

        if (--nest.releaseCooldown > 0) {
            return;
        }

        nest.releaseCooldown = 80 + level.random.nextInt(100);
        nest.releaseOne(level);
    }

    public void populateInitialWasps(ServerLevel level) {
        if (this.initialized) {
            return;
        }

        this.initialized = true;
        if (!this.nestBlock().shouldPopulateInitialWasps()) {
            return;
        }

        this.pandaVariant = level.random.nextInt(PANDA_VARIANT_CHANCE) == 0;
        int amount = 1 + level.random.nextInt(3);
        for (int i = 0; i < amount && this.storedWasps.size() < this.maxOccupants(); i++) {
            this.storedWasps.add(this.createStoredWasp(level, this.nestBlock().waspType()));
        }
        this.setChanged();
    }

    public boolean tryStoreWasp(WaspEntity waspEntity) {
        if (this.storedWasps.size() >= this.maxOccupants()) {
            return false;
        }

        waspEntity.setPandaVariant(this.pandaVariant);
        CompoundTag waspTag = new CompoundTag();
        waspEntity.addAdditionalSaveData(waspTag);
        this.storedWasps.add(new StoredWasp(waspEntity.getType(), waspTag));
        this.releaseCooldown = Math.max(this.releaseCooldown, 200);
        waspEntity.discard();
        this.setChanged();
        return true;
    }

    public void releaseAll(ServerLevel level) {
        while (!this.storedWasps.isEmpty()) {
            this.releaseOne(level);
        }
    }

    private void releaseOne(ServerLevel level) {
        if (this.storedWasps.isEmpty()) {
            return;
        }

        StoredWasp storedWasp = this.storedWasps.get(0);
        WaspEntity waspEntity = storedWasp.create(level);
        if (waspEntity == null) {
            this.storedWasps.remove(0);
            return;
        }

        BlockPos spawnPos = this.findReleasePos(level);
        if (spawnPos == null) {
            this.storedWasps.remove(0);
            this.setChanged();
            return;
        }

        waspEntity.moveTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 0.05D,
                spawnPos.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        waspEntity.setHivePos(this.getBlockPos());
        if (!level.noCollision(waspEntity)) {
            this.storedWasps.remove(0);
            this.setChanged();
            return;
        }

        this.storedWasps.remove(0);
        level.addFreshEntity(waspEntity);
        this.setChanged();
    }

    private int maxOccupants() {
        return this.nestBlock().maxOccupants();
    }

    private WaspNestBlock nestBlock() {
        return (WaspNestBlock) this.getBlockState().getBlock();
    }

    private BlockPos findReleasePos(ServerLevel level) {
        Direction facing = this.getBlockState().getValue(WaspNestBlock.FACING);
        BlockPos front = this.getBlockPos().relative(facing);
        if (level.getBlockState(front).canBeReplaced() && level.getBlockState(front.above()).canBeReplaced()) {
            return front;
        }

        BlockPos above = this.getBlockPos().above();
        if (level.getBlockState(above).canBeReplaced() && level.getBlockState(above.above()).canBeReplaced()) {
            return above;
        }

        return null;
    }

    private StoredWasp createStoredWasp(ServerLevel level, EntityType<?> waspType) {
        if (!(waspType.create(level) instanceof WaspEntity waspEntity)) {
            return new StoredWasp(waspType, new CompoundTag());
        }

        waspEntity.setPandaVariant(this.pandaVariant);
        CompoundTag waspTag = new CompoundTag();
        waspEntity.addAdditionalSaveData(waspTag);
        return new StoredWasp(waspType, waspTag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Initialized", this.initialized);
        tag.putBoolean(PANDA_VARIANT, this.pandaVariant);
        ListTag listTag = new ListTag();
        for (StoredWasp storedWasp : this.storedWasps) {
            listTag.add(storedWasp.save());
        }
        tag.put(STORED_WASPS, listTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.initialized = tag.getBoolean("Initialized");
        this.pandaVariant = tag.getBoolean(PANDA_VARIANT);
        this.storedWasps.clear();
        ListTag listTag = tag.getList(STORED_WASPS, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            this.storedWasps.add(StoredWasp.load(listTag.getCompound(i)));
        }
    }

    private record StoredWasp(EntityType<?> waspType, CompoundTag data) {
        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(this.waspType).toString());
            tag.put("data", this.data.copy());
            return tag;
        }

        private static StoredWasp load(CompoundTag tag) {
            EntityType<?> waspType = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(tag.getString("id")))
                    .orElse(AntarchyObjects.WASP.get());
            return new StoredWasp(waspType, tag.getCompound("data"));
        }

        private WaspEntity create(ServerLevel level) {
            if (!(this.waspType.create(level) instanceof WaspEntity waspEntity)) {
                return null;
            }

            waspEntity.readAdditionalSaveData(this.data.copy());
            return waspEntity;
        }
    }
}
