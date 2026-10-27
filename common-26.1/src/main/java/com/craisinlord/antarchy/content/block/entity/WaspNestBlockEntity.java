package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.WaspNestBlock;
import com.craisinlord.antarchy.content.entity.WaspEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

        if (level.isDarkOutside() || level.isRaining()) {
            return;
        }

        if (nest.storedWasps.isEmpty()) {
            return;
        }

        if (--nest.releaseCooldown > 0) {
            return;
        }

        nest.releaseCooldown = 80 + level.getRandom().nextInt(100);
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

        this.pandaVariant = level.getRandom().nextInt(PANDA_VARIANT_CHANCE) == 0;
        int amount = 1 + level.getRandom().nextInt(3);
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
        CompoundTag waspTag = saveWaspData(waspEntity);
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

        waspEntity.snapTo(
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + 0.05D,
                spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F,
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
        if (!(waspType.create(level, EntitySpawnReason.TRIGGERED) instanceof WaspEntity waspEntity)) {
            return new StoredWasp(waspType, new CompoundTag());
        }

        waspEntity.setPandaVariant(this.pandaVariant);
        CompoundTag waspTag = saveWaspData(waspEntity);
        return new StoredWasp(waspType, waspTag);
    }

    private static CompoundTag saveWaspData(WaspEntity waspEntity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, waspEntity.registryAccess());
        waspEntity.addAdditionalSaveData(output);
        return output.buildResult();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Initialized", this.initialized);
        output.putBoolean(PANDA_VARIANT, this.pandaVariant);
        ValueOutput.TypedOutputList<CompoundTag> listTag = output.list(STORED_WASPS, CompoundTag.CODEC);
        for (StoredWasp storedWasp : this.storedWasps) {
            listTag.add(storedWasp.save());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.initialized = input.getBooleanOr("Initialized", false);
        this.pandaVariant = input.getBooleanOr(PANDA_VARIANT, false);
        this.storedWasps.clear();
        for (CompoundTag waspTag : input.listOrEmpty(STORED_WASPS, CompoundTag.CODEC)) {
            this.storedWasps.add(StoredWasp.load(waspTag));
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
            EntityType<?> waspType = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(tag.getStringOr("id", "")))
                    .orElse(AntarchyObjects.WASP.get());
            return new StoredWasp(waspType, tag.getCompoundOrEmpty("data"));
        }

        private WaspEntity create(ServerLevel level) {
            if (!(this.waspType.create(level, EntitySpawnReason.LOAD) instanceof WaspEntity waspEntity)) {
                return null;
            }

            waspEntity.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), this.data.copy()));
            return waspEntity;
        }
    }
}
