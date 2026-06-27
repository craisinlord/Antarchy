package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;

public class TermiteEntity extends BaseAntEntity implements GeoEntity {
    private static final int WOOD_BITE_ANIMATION_TICKS = 8;
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
    protected boolean canGroupWithNestmates() {
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
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
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
        BlockPos basePos = this.blockPosition();
        for (Vec3i offset : IMMEDIATE_WOOD_OFFSETS) {
            BlockPos targetPos = basePos.offset(offset);
            BlockState targetState = this.level().getBlockState(targetPos);
            if (!targetState.is(AntarchyTags.Blocks.TERMITE_FOODS)) {
                continue;
            }

            SoundType soundType = targetState.getSoundType();
            this.triggerBiteAnimation(WOOD_BITE_ANIMATION_TICKS);
            this.level().destroyBlock(targetPos, false, this);
            this.level().playSound(null, targetPos, soundType.getBreakSound(), this.getSoundSource(), 0.7F, 1.1F);
            return true;
        }

        return false;
    }

    @Nullable
    private BlockPos getCurrentWoodTarget() {
        if (this.targetWoodPos != null && this.isValidWoodTarget(this.targetWoodPos)) {
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
        return this.level().getBlockState(targetPos).is(AntarchyTags.Blocks.TERMITE_FOODS);
    }

    @Nullable
    private Vec3 getWoodApproachTarget(BlockPos targetPos) {
        BlockPos belowTarget = targetPos.below();
        if (this.isWalkableStandPos(belowTarget)) {
            return Vec3.atBottomCenterOf(belowTarget);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = targetPos.relative(direction);
            if (this.isWalkableStandPos(sidePos)) {
                return Vec3.atBottomCenterOf(sidePos);
            }

            BlockPos belowSidePos = sidePos.below();
            if (this.isWalkableStandPos(belowSidePos)) {
                return Vec3.atBottomCenterOf(belowSidePos);
            }
        }

        return null;
    }

    private boolean isWalkableStandPos(BlockPos pos) {
        BlockPos floor = pos.below();
        BlockState floorState = this.level().getBlockState(floor);
        return this.level().getBlockState(pos).isAir()
                && this.level().getBlockState(pos.above()).isAir()
                && floorState.isFaceSturdy(this.level(), floor, Direction.UP);
    }
}
