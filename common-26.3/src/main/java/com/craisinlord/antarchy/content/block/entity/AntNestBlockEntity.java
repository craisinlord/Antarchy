package com.craisinlord.antarchy.content.block.entity;

import net.minecraft.world.entity.EntityTypes;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.AntNestBlock;
import com.craisinlord.antarchy.content.block.AntTrapBlock;
import com.craisinlord.antarchy.content.entity.ant.BaseAntEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
        if (state.getBlock() instanceof com.craisinlord.antarchy.content.block.AntTrapBlock) {
            if (level.getGameTime() % 20L == 0L && nest.storedAnts.size() < nest.maxOccupants()) {
                net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(pos).inflate(15.0D);
                for (BaseAntEntity ant : level.getEntitiesOfClass(BaseAntEntity.class, searchArea, BaseAntEntity::isAlive)) {
                    if (ant.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > 225.0D) {
                        continue;
                    }
                    if (!nest.canAccept(ant.getType())) {
                        continue;
                    }
                    if (ant.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= 2.25D) {
                        nest.tryStoreAnt(ant);
                        if (nest.storedAnts.size() >= nest.maxOccupants()) {
                            break;
                        }
                    } else {
                        ant.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.05D);
                    }
                }
            }
            return;
        }
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
        if ((!ignoreNight && level.isDarkOutside()) || level.isRaining()) {
            return;
        }

        if (nest.storedAnts.isEmpty()) {
            return;
        }

        if (--nest.releaseCooldown > 0) {
            return;
        }

        nest.releaseCooldown = RELEASE_COOLDOWN_MIN + level.getRandom().nextInt(RELEASE_COOLDOWN_RANGE);
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
        int amount = INITIAL_ANT_COUNT_MIN + level.getRandom().nextInt(INITIAL_ANT_COUNT_RANGE);
        for (int i = 0; i < amount && this.storedAnts.size() < this.maxOccupants(); i++) {
            EntityType<?> antType = this.nestBlock().initialAntType(level.getRandom());
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
        CompoundTag antTag = saveAntData(antEntity);
        this.storedAnts.add(new StoredAnt(antEntity.getType(), antTag));
        this.releaseCooldown = Math.max(this.releaseCooldown, ENTER_NEST_COOLDOWN);
        antEntity.discard();
        this.setChanged();
        return true;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (state.getBlock() instanceof AntTrapBlock && this.level instanceof ServerLevel serverLevel) {
            this.releaseAll(serverLevel);
        }
    }

    public void releaseAll(ServerLevel level) {
        int remaining = this.storedAnts.size();
        while (remaining-- > 0 && !this.storedAnts.isEmpty()) {
            this.releaseOne(level, true);
        }
    }

    private void releaseOne(ServerLevel level) {
        this.releaseOne(level, false);
    }

    private void releaseOne(ServerLevel level, boolean forceRelease) {
        if (this.storedAnts.isEmpty()) {
            return;
        }

        StoredAnt storedAnt = this.storedAnts.get(0);
        BaseAntEntity antEntity = storedAnt.create(level);
        if (antEntity == null) {
            this.storedAnts.remove(0);
            return;
        }

        BlockPos origin = this.getBlockPos();
        java.util.List<BlockPos> candidates = new java.util.ArrayList<>();
        for (int y = 1; y <= 3; y++) {
            candidates.add(origin.above(y));
            for (int radius = 1; radius <= 2; radius++) {
                candidates.add(origin.offset(radius, y, 0));
                candidates.add(origin.offset(-radius, y, 0));
                candidates.add(origin.offset(0, y, radius));
                candidates.add(origin.offset(0, y, -radius));
            }
        }
        BlockPos spawnPos = null;
        for (BlockPos candidate : candidates) {
            antEntity.snapTo(candidate.getX() + SPAWN_OFFSET_XZ, candidate.getY() + SPAWN_OFFSET_Y, candidate.getZ() + SPAWN_OFFSET_XZ,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(antEntity)) {
                spawnPos = candidate;
                break;
            }
        }
        if (spawnPos == null && !forceRelease) {
            return;
        }
        if (spawnPos == null) {
            antEntity.snapTo(origin.getX() + SPAWN_OFFSET_XZ, origin.getY() + SPAWN_OFFSET_Y, origin.getZ() + SPAWN_OFFSET_XZ,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
        }
        antEntity.setNestPos(origin);
        antEntity.onExitNest();
        if (!level.addFreshEntity(antEntity) && !forceRelease) {
            return;
        }
        this.storedAnts.remove(0);
        this.setChanged();
    }

    private int maxOccupants() {
        return this.nestBlock().maxOccupants();
    }

    private AntNestBlock nestBlock() {
        return (AntNestBlock) this.getBlockState().getBlock();
    }

    private StoredAnt createStoredAnt(ServerLevel level, EntityType<?> antType) {
        if (!(antType.create(level, EntitySpawnReason.TRIGGERED) instanceof BaseAntEntity antEntity)) {
            return new StoredAnt(antType, new CompoundTag());
        }

        antEntity.setNestPos(this.getBlockPos());
        CompoundTag antTag = saveAntData(antEntity);
        return new StoredAnt(antType, antTag);
    }

    private static CompoundTag saveAntData(BaseAntEntity antEntity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, antEntity.registryAccess());
        antEntity.addAdditionalSaveData(output);
        return output.buildResult();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(NBT_INITIALIZED, this.initialized);
        output.putInt(NBT_REBROOD_COOLDOWN, this.rebroodCooldown);
        ValueOutput.TypedOutputList<CompoundTag> listTag = output.list(STORED_ANTS, CompoundTag.CODEC);
        for (StoredAnt storedAnt : this.storedAnts) {
            listTag.add(storedAnt.save());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.initialized = input.getBooleanOr(NBT_INITIALIZED, false);
        this.rebroodCooldown = input.getIntOr(NBT_REBROOD_COOLDOWN, REBROOD_COOLDOWN_TICKS);
        this.storedAnts.clear();
        for (CompoundTag antTag : input.listOrEmpty(STORED_ANTS, CompoundTag.CODEC)) {
            this.storedAnts.add(StoredAnt.load(antTag));
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
            EntityType<?> antType = BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.Identifier.parse(tag.getStringOr("id", ""))).orElse(EntityTypes.SILVERFISH);
            return new StoredAnt(antType, tag.getCompoundOrEmpty("data"));
        }

        private BaseAntEntity create(ServerLevel level) {
            if (!(this.antType.create(level, EntitySpawnReason.LOAD) instanceof BaseAntEntity antEntity)) {
                return null;
            }

            antEntity.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), this.data.copy()));
            return antEntity;
        }
    }
}
