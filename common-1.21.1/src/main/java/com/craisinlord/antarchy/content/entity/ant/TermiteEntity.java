package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public class TermiteEntity extends BaseAntEntity implements GeoEntity {
    private static final int WOOD_BITE_ANIMATION_TICKS = 8;
    private static final int WOOD_BITE_COOLDOWN_TICKS = 20 * 3;
    private static final int WOOD_SEARCH_INTERVAL_TICKS = 20;
    private static final int WOOD_REPATH_INTERVAL_TICKS = 6;
    private static final int WOOD_SEARCH_RADIUS_HORIZONTAL = 12;
    private static final int WOOD_SEARCH_RADIUS_VERTICAL = 4;
    private static final Vec3i[] IMMEDIATE_WOOD_OFFSETS = {
        new Vec3i(0, 1, 0),
        new Vec3i(0, 0, -1),
        new Vec3i(0, 0, 1),
        new Vec3i(1, 0, 0),
        new Vec3i(-1, 0, 0),
        new Vec3i(0, 1, -1),
        new Vec3i(0, 1, 1),
        new Vec3i(1, 1, 0),
        new Vec3i(-1, 1, 0),
    };
    @Nullable
    private BlockPos targetWoodPos;
    private int nextWoodBiteTick;
    private int nextWoodSearchTick;
    private int nextWoodRepathTick;

    public TermiteEntity(EntityType<? extends BaseAntEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.targetWoodPos != null && !this.isValidWoodTarget(this.targetWoodPos)) {
            this.targetWoodPos = null;
        }
    }

    @Override
    protected ResourceKey<Level> destinationDimension() {
        return AntarchySettings.termiteDestinationDimension();
    }

    @Override
    protected TagKey<Item> activationItemsTag() {
        return AntarchyTags.Items.TERMITE_ACTIVATION_ITEMS;
    }

    @Override
    protected TagKey<Item> breedingFoodsTag() {
        return AntarchyTags.Items.TERMITE_BREEDING_FOODS;
    }

    @Override
    protected boolean requiresActivationReagent() {
        return AntarchySettings.termiteRequiresReagent();
    }

    @Override
    protected String activationMessageKey() {
        return "message.antarchy.termite_activated";
    }

    @Override
    protected String needsReagentMessageKey() {
        return "message.antarchy.termite_needs_reagent";
    }

    @Override
    protected double configuredMaxHealth() {
        return AntarchySettings.termiteHealth();
    }

    @Override
    protected boolean canForageGroundFood() {
        return false;
    }

    @Override
    protected boolean canMarch() {
        return false;
    }

    @Override
    protected boolean shouldUseBiteAnimation() {
        return true;
    }

    @Override
    protected boolean handlePriorityForaging() {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                || !this.level().getGameRules().getBoolean(AntarchyGameRules.RULE_DO_TERMITE_GREIFING)) {
            this.targetWoodPos = null;
            return false;
        }

        if (this.tryEatNearbyWood()) {
            this.targetWoodPos = null;
            return true;
        }

        BlockPos targetPos = this.getCurrentWoodTarget();
        if (targetPos == null) {
            return false;
        }

        Vec3 moveTarget = this.getWoodApproachTarget(targetPos);
        if (moveTarget == null) {
            this.targetWoodPos = null;
            return false;
        }

        if (this.getNavigation().isDone() || this.tickCount >= this.nextWoodRepathTick) {
            this.getNavigation().moveTo(moveTarget.x, moveTarget.y, moveTarget.z, 1.22D);
            this.nextWoodRepathTick = this.tickCount + WOOD_REPATH_INTERVAL_TICKS;
        }
        return true;
    }

    private boolean tryEatNearbyWood() {
        if (this.tickCount < this.nextWoodBiteTick) {
            return false;
        }

        BlockPos basePos = this.blockPosition();
        for (Vec3i offset : IMMEDIATE_WOOD_OFFSETS) {
            BlockPos targetPos = basePos.offset(offset);
            BlockState targetState = this.level().getBlockState(targetPos);
            if (!this.isTermiteEdible(targetState)) {
                continue;
            }

            if (this.tryStripWood(targetPos, targetState)) {
                return true;
            }

            if (targetState.is(AntarchyTags.Blocks.TERMITE_FOODS) || this.isStrippedWoodState(targetState)) {
                SoundType soundType = targetState.getSoundType();
                this.triggerBiteAnimation(WOOD_BITE_ANIMATION_TICKS);
                this.nextWoodBiteTick = this.tickCount + WOOD_BITE_COOLDOWN_TICKS;
                this.level().destroyBlock(targetPos, false, this);
                this.level().playSound(null, targetPos, soundType.getBreakSound(), this.getSoundSource(), 0.7F, 1.1F);
                this.playSound(AntarchySoundEvents.ANT_BITE.get(), 0.35F, 0.95F + this.random.nextFloat() * 0.1F);
                return true;
            }
        }

        return false;
    }

    @Nullable
    private BlockPos getCurrentWoodTarget() {
        if (this.targetWoodPos != null && this.isValidWoodTarget(this.targetWoodPos) && this.canReachWoodTarget(this.targetWoodPos)) {
            return this.targetWoodPos;
        }

        if (this.tickCount < this.nextWoodSearchTick) {
            return null;
        }

        this.nextWoodSearchTick = this.tickCount + WOOD_SEARCH_INTERVAL_TICKS;
        this.targetWoodPos = this.findClosestWoodTarget();
        return this.targetWoodPos;
    }

    @Nullable
    private BlockPos findClosestWoodTarget() {
        BlockPos origin = this.blockPosition();
        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        BlockPos bestPos = null;
        double bestDistSq = Double.MAX_VALUE;
        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();

        for (int r = 0; r <= WOOD_SEARCH_RADIUS_HORIZONTAL; r++) {
            if (bestPos != null && bestDistSq <= (double)(r * r)) {
                break;
            }
            for (int dy = -WOOD_SEARCH_RADIUS_VERTICAL; dy <= WOOD_SEARCH_RADIUS_VERTICAL + 2; dy++) {
                if (r == 0) {
                    candidate.set(ox, oy + dy, oz);
                    if (this.isValidWoodTarget(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDistSq) { bestDistSq = d; bestPos = candidate.immutable(); }
                    }
                    continue;
                }
                // North/South faces (dz = ±r)
                for (int dx = -r; dx <= r; dx++) {
                    candidate.set(ox + dx, oy + dy, oz - r);
                    if (this.isValidWoodTarget(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDistSq) { bestDistSq = d; bestPos = candidate.immutable(); }
                    }
                    candidate.set(ox + dx, oy + dy, oz + r);
                    if (this.isValidWoodTarget(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDistSq) { bestDistSq = d; bestPos = candidate.immutable(); }
                    }
                }
                // East/West faces (dx = ±r, corners already covered above)
                for (int dz = -r + 1; dz <= r - 1; dz++) {
                    candidate.set(ox - r, oy + dy, oz + dz);
                    if (this.isValidWoodTarget(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDistSq) { bestDistSq = d; bestPos = candidate.immutable(); }
                    }
                    candidate.set(ox + r, oy + dy, oz + dz);
                    if (this.isValidWoodTarget(candidate)) {
                        double d = candidate.distSqr(origin);
                        if (d < bestDistSq) { bestDistSq = d; bestPos = candidate.immutable(); }
                    }
                }
            }
        }

        return bestPos;
    }

    private boolean isValidWoodTarget(BlockPos targetPos) {
        return this.isTermiteEdible(this.level().getBlockState(targetPos));
    }

    private boolean isTermiteEdible(BlockState state) {
        return state.is(AntarchyTags.Blocks.TERMITE_FOODS) || this.isStrippedWoodState(state);
    }

    private boolean tryStripWood(BlockPos targetPos, BlockState targetState) {
        BlockState strippedState = this.getStrippedState(targetState);
        if (strippedState == null) {
            return false;
        }

        SoundType soundType = targetState.getSoundType();
        this.triggerBiteAnimation(WOOD_BITE_ANIMATION_TICKS);
        this.nextWoodBiteTick = this.tickCount + WOOD_BITE_COOLDOWN_TICKS;
        this.level().setBlock(targetPos, strippedState, Block.UPDATE_ALL_IMMEDIATE);
        this.level().playSound(null, targetPos, soundType.getBreakSound(), this.getSoundSource(), 0.6F, 1.2F);
        this.playSound(AntarchySoundEvents.ANT_BITE.get(), 0.35F, 0.95F + this.random.nextFloat() * 0.1F);
        return true;
    }

    @Nullable
    private BlockState getStrippedState(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId == null || this.isStrippedWoodState(state) || !this.isStripTargetState(state, blockId.getPath())) {
            return null;
        }

        String path = blockId.getPath();
        if (path.startsWith("mossy_")) {
            path = path.substring("mossy_".length());
        }

        ResourceLocation strippedId = ResourceLocation.fromNamespaceAndPath(blockId.getNamespace(), "stripped_" + path);
        Block strippedBlock = BuiltInRegistries.BLOCK.getOptional(strippedId).orElse(null);
        if (strippedBlock == null) {
            return null;
        }

        BlockState strippedState = strippedBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS) && strippedState.hasProperty(RotatedPillarBlock.AXIS)) {
            strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        return strippedState;
    }

    private boolean isStripTargetState(BlockState state, String path) {
        return state.is(BlockTags.LOGS) || path.endsWith("_wood");
    }

    private boolean isStrippedWoodState(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId == null) {
            return false;
        }

        String path = blockId.getPath();
        return path.startsWith("stripped_") && (path.endsWith("_log") || path.endsWith("_wood") || path.endsWith("_stem") || path.endsWith("_hyphae"));
    }

    private boolean canReachWoodTarget(BlockPos targetPos) {
        BlockPos approachPos = this.getWoodApproachBlockPos(targetPos);
        if (approachPos == null) {
            return false;
        }

        Path path = this.getNavigation().createPath(approachPos, 0);
        return path != null && path.canReach();
    }

    @Nullable
    private BlockPos getWoodApproachBlockPos(BlockPos targetPos) {
        BlockPos belowTarget = targetPos.below();
        if (this.isWalkableStandPos(belowTarget)) {
            return belowTarget;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = targetPos.relative(direction);
            if (this.isWalkableStandPos(sidePos)) {
                return sidePos;
            }

            BlockPos belowSidePos = sidePos.below();
            if (this.isWalkableStandPos(belowSidePos)) {
                return belowSidePos;
            }
        }

        return null;
    }

    @Nullable
    private Vec3 getWoodApproachTarget(BlockPos targetPos) {
        BlockPos approachPos = this.getWoodApproachBlockPos(targetPos);
        return approachPos == null ? null : Vec3.atBottomCenterOf(approachPos);
    }

    private boolean isWalkableStandPos(BlockPos pos) {
        BlockPos floor = pos.below();
        BlockState floorState = this.level().getBlockState(floor);
        return this.level().getBlockState(pos).isAir()
                && this.level().getBlockState(pos.above()).isAir()
                && floorState.isFaceSturdy(this.level(), floor, Direction.UP);
    }
}
