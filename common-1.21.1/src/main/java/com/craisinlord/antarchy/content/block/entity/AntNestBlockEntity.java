package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.AntNestBlock;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AntNestBlockEntity extends BlockEntity {
    private static final String STORED_ANTS = "StoredAnts";
    private static final String NBT_INITIALIZED = "Initialized";
    private static final String NBT_REBROOD_COOLDOWN = "RebroodCooldown";
    private static final int RELEASE_COOLDOWN_MIN = 80;
    private static final int RELEASE_COOLDOWN_RANGE = 100;
    private static final int ENTER_NEST_COOLDOWN = 200;
    private static final int INITIAL_ANT_COUNT_MIN = 2;
    private static final int INITIAL_ANT_COUNT_RANGE = 4;
    private static final int REBROOD_COOLDOWN_TICKS = 20 * 60 * 5;
    private static final double PLAYER_ACTIVATION_RANGE = 16.0D;
    private static final double SPAWN_OFFSET_XZ = 0.5D;
    private static final double SPAWN_OFFSET_Y = 0.05D;

    private final List<StoredAnt> storedAnts = new ArrayList<>();
    private int releaseCooldown = RELEASE_COOLDOWN_MIN;
    private int rebroodCooldown = REBROOD_COOLDOWN_TICKS;
    private boolean initialized = false;

    public AntNestBlockEntity(BlockPos pos, BlockState blockState) {
        super(AntarchyObjects.ANT_NEST_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AntNestBlockEntity nest) {
        boolean playerNearby = level.hasNearbyAlivePlayer(
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, PLAYER_ACTIVATION_RANGE);
        if (!playerNearby) {
            return;
        }

        if (!nest.initialized) {
            nest.populateInitialAnts(level);
        } else if (nest.storedAnts.isEmpty() && nest.nestBlock().shouldPopulateInitialAnts()) {
            if (--nest.rebroodCooldown <= 0) {
                nest.rebroodCooldown = REBROOD_COOLDOWN_TICKS;
                nest.fillWithAnts(level);
            }
        }

        boolean ignoreNight = level.dimensionType().hasFixedTime();
        if ((!ignoreNight && level.isNight()) || level.isRaining()) {
            return;
        }

        if (nest.storedAnts.isEmpty()) {
            return;
        }

        if (--nest.releaseCooldown > 0) {
            return;
        }

        nest.releaseCooldown = RELEASE_COOLDOWN_MIN + level.random.nextInt(RELEASE_COOLDOWN_RANGE);
        nest.releaseOne(level);
    }

    public void populateInitialAnts(ServerLevel level) {
        if (this.initialized) {
            return;
        }

        this.initialized = true;
        if (!this.nestBlock().shouldPopulateInitialAnts()) {
            return;
        }

        this.fillWithAnts(level);
    }

    private void fillWithAnts(ServerLevel level) {
        int amount = INITIAL_ANT_COUNT_MIN + level.random.nextInt(INITIAL_ANT_COUNT_RANGE);
        for (int i = 0; i < amount && this.storedAnts.size() < this.maxOccupants(); i++) {
            EntityType<?> antType = this.nestBlock().initialAntType(level.random);
            this.storedAnts.add(this.createStoredAnt(level, antType));
        }
        this.rebroodCooldown = REBROOD_COOLDOWN_TICKS;
        this.setChanged();
    }

    public boolean canAccept(EntityType<?> antType) {
        return this.nestBlock().canAccept(antType) && this.storedAnts.size() < this.maxOccupants();
    }

    public boolean tryStoreAnt(BaseAntEntity antEntity) {
        if (!this.canAccept(antEntity.getType())) {
            return false;
        }

        antEntity.onEnterNest();
        CompoundTag antTag = new CompoundTag();
        antEntity.addAdditionalSaveData(antTag);
        this.storedAnts.add(new StoredAnt(antEntity.getType(), antTag));
        this.releaseCooldown = Math.max(this.releaseCooldown, ENTER_NEST_COOLDOWN);
        antEntity.discard();
        this.setChanged();
        return true;
    }

    public void releaseAll(ServerLevel level) {
        while (!this.storedAnts.isEmpty()) {
            this.releaseOne(level);
        }
    }

    private void releaseOne(ServerLevel level) {
        if (this.storedAnts.isEmpty()) {
            return;
        }

        StoredAnt storedAnt = this.storedAnts.get(0);
        BaseAntEntity antEntity = storedAnt.create(level);
        if (antEntity == null) {
            this.storedAnts.remove(0);
            return;
        }

        BlockPos spawnPos = this.getBlockPos().above();
        if (!level.getBlockState(spawnPos).canBeReplaced()) {
            return;
        }

        antEntity.moveTo(spawnPos.getX() + SPAWN_OFFSET_XZ, spawnPos.getY() + SPAWN_OFFSET_Y, spawnPos.getZ() + SPAWN_OFFSET_XZ, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(antEntity)) {
            return;
        }

        this.storedAnts.remove(0);
        antEntity.setNestPos(this.getBlockPos());
        antEntity.onExitNest();
        level.addFreshEntity(antEntity);
        this.setChanged();
    }

    private int maxOccupants() {
        return this.nestBlock().maxOccupants();
    }

    private AntNestBlock nestBlock() {
        return (AntNestBlock) this.getBlockState().getBlock();
    }

    private StoredAnt createStoredAnt(ServerLevel level, EntityType<?> antType) {
        if (!(antType.create(level) instanceof BaseAntEntity antEntity)) {
            return new StoredAnt(antType, new CompoundTag());
        }

        antEntity.setNestPos(this.getBlockPos());
        CompoundTag antTag = new CompoundTag();
        antEntity.addAdditionalSaveData(antTag);
        return new StoredAnt(antType, antTag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean(NBT_INITIALIZED, this.initialized);
        tag.putInt(NBT_REBROOD_COOLDOWN, this.rebroodCooldown);
        ListTag listTag = new ListTag();
        for (StoredAnt storedAnt : this.storedAnts) {
            listTag.add(storedAnt.save());
        }
        tag.put(STORED_ANTS, listTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.initialized = tag.getBoolean(NBT_INITIALIZED);
        this.rebroodCooldown = tag.contains(NBT_REBROOD_COOLDOWN) ? tag.getInt(NBT_REBROOD_COOLDOWN) : REBROOD_COOLDOWN_TICKS;
        this.storedAnts.clear();
        ListTag listTag = tag.getList(STORED_ANTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            this.storedAnts.add(StoredAnt.load(listTag.getCompound(i)));
        }
    }

    private record StoredAnt(EntityType<?> antType, CompoundTag data) {
        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(this.antType).toString());
            tag.put("data", this.data.copy());
            return tag;
        }

        private static StoredAnt load(CompoundTag tag) {
            EntityType<?> antType = BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.parse(tag.getString("id"))).orElse(EntityType.SILVERFISH);
            return new StoredAnt(antType, tag.getCompound("data"));
        }

        private BaseAntEntity create(ServerLevel level) {
            if (!(this.antType.create(level) instanceof BaseAntEntity antEntity)) {
                return null;
            }

            antEntity.readAdditionalSaveData(this.data.copy());
            return antEntity;
        }
    }
}
