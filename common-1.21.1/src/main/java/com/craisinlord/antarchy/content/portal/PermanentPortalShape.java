package com.craisinlord.antarchy.content.portal;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PermanentPortalShape {
    public static final int MIN_WIDTH = 2;
    public static final int MIN_HEIGHT = 3;
    public static final int MAX_SIZE = 21;
    public static final int ACTIVE_SEARCH_RADIUS = 24;

    private final TagKey<Block> frameTag;
    private final Block portalBlock;
    private final Direction.Axis axis;
    private final BlockPos bottomLeft;
    private final int width;
    private final int height;

    private PermanentPortalShape(TagKey<Block> frameTag, Block portalBlock, Direction.Axis axis, BlockPos bottomLeft, int width, int height) {
        this.frameTag = frameTag;
        this.portalBlock = portalBlock;
        this.axis = axis;
        this.bottomLeft = bottomLeft;
        this.width = width;
        this.height = height;
    }

    @Nullable
    public static PermanentPortalShape findInactive(BlockGetter level, BlockPos interiorPos, TagKey<Block> frameTag, Block portalBlock, Direction.Axis axis) {
        return find(level, interiorPos, frameTag, portalBlock, axis, state -> state.isAir());
    }

    @Nullable
    public static PermanentPortalShape findInactive(BlockGetter level, BlockPos interiorPos, PermanentPortalType type, Direction.Axis axis) {
        return findInactive(level, interiorPos, type.frameTag(), type.portalBlock(), axis);
    }

    @Nullable
    public static PermanentPortalShape findInactiveNear(BlockGetter level, BlockPos center, TagKey<Block> frameTag, Block portalBlock) {
        for (int x = center.getX() - 2; x <= center.getX() + 2; x++) {
            for (int y = center.getY() - 2; y <= center.getY() + 2; y++) {
                for (int z = center.getZ() - 2; z <= center.getZ() + 2; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    PermanentPortalShape xShape = findInactive(level, candidate, frameTag, portalBlock, Direction.Axis.X);
                    if (xShape != null) {
                        return xShape;
                    }
                    PermanentPortalShape zShape = findInactive(level, candidate, frameTag, portalBlock, Direction.Axis.Z);
                    if (zShape != null) {
                        return zShape;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static PermanentPortalShape findInactiveNear(BlockGetter level, BlockPos center, PermanentPortalType type) {
        return findInactiveNear(level, center, type.frameTag(), type.portalBlock());
    }

    @Nullable
    public static PermanentPortalShape findActive(BlockGetter level, BlockPos interiorPos, PermanentPortalType type, Direction.Axis axis) {
        Block portalBlock = type.portalBlock();
        return find(level, interiorPos, type.frameTag(), portalBlock, axis, state -> state.is(portalBlock));
    }

    @Nullable
    public static PermanentPortalShape findActiveNear(BlockGetter level, BlockPos center, PermanentPortalType type) {
        for (int x = center.getX() - ACTIVE_SEARCH_RADIUS; x <= center.getX() + ACTIVE_SEARCH_RADIUS; x++) {
            for (int y = center.getY() - ACTIVE_SEARCH_RADIUS; y <= center.getY() + ACTIVE_SEARCH_RADIUS; y++) {
                for (int z = center.getZ() - ACTIVE_SEARCH_RADIUS; z <= center.getZ() + ACTIVE_SEARCH_RADIUS; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    PermanentPortalShape xShape = findActive(level, candidate, type, Direction.Axis.X);
                    if (xShape != null) {
                        return xShape;
                    }
                    PermanentPortalShape zShape = findActive(level, candidate, type, Direction.Axis.Z);
                    if (zShape != null) {
                        return zShape;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private static PermanentPortalShape find(
            BlockGetter level,
            BlockPos interiorPos,
            TagKey<Block> frameTag,
            Block portalBlock,
            Direction.Axis axis,
            Predicate<BlockState> interiorPredicate
    ) {
        if (axis == Direction.Axis.Y || !interiorPredicate.test(level.getBlockState(interiorPos))) {
            return null;
        }

        Direction widthDirection = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Direction leftDirection = widthDirection.getOpposite();
        BlockPos cursor = interiorPos;

        for (int i = 0; i < MAX_SIZE && interiorPredicate.test(level.getBlockState(cursor.below())); i++) {
            cursor = cursor.below();
        }

        for (int i = 0; i < MAX_SIZE && interiorPredicate.test(level.getBlockState(cursor.relative(leftDirection))); i++) {
            cursor = cursor.relative(leftDirection);
        }

        int width = measureWidth(level, cursor, widthDirection, frameTag, interiorPredicate);
        if (width < MIN_WIDTH) {
            return null;
        }

        int height = measureHeight(level, cursor, widthDirection, width, frameTag, interiorPredicate);
        if (height < MIN_HEIGHT) {
            return null;
        }

        return new PermanentPortalShape(frameTag, portalBlock, axis, cursor.immutable(), width, height);
    }

    private static int measureWidth(
            BlockGetter level,
            BlockPos bottomLeft,
            Direction widthDirection,
            TagKey<Block> frameTag,
            Predicate<BlockState> interiorPredicate
    ) {
        if (!level.getBlockState(bottomLeft.relative(widthDirection.getOpposite())).is(frameTag)) {
            return 0;
        }

        int width = 0;
        while (width < MAX_SIZE && interiorPredicate.test(level.getBlockState(bottomLeft.relative(widthDirection, width)))) {
            if (!level.getBlockState(bottomLeft.relative(widthDirection, width).below()).is(frameTag)) {
                return 0;
            }
            width++;
        }

        if (width < MIN_WIDTH || width > MAX_SIZE) {
            return 0;
        }

        return level.getBlockState(bottomLeft.relative(widthDirection, width)).is(frameTag) ? width : 0;
    }

    private static int measureHeight(
            BlockGetter level,
            BlockPos bottomLeft,
            Direction widthDirection,
            int width,
            TagKey<Block> frameTag,
            Predicate<BlockState> interiorPredicate
    ) {
        BlockPos leftFrameBase = bottomLeft.relative(widthDirection.getOpposite());
        BlockPos rightFrameBase = bottomLeft.relative(widthDirection, width);

        for (int height = 0; height < MAX_SIZE; height++) {
            if (!level.getBlockState(leftFrameBase.above(height)).is(frameTag)
                    || !level.getBlockState(rightFrameBase.above(height)).is(frameTag)) {
                return 0;
            }

            for (int x = 0; x < width; x++) {
                BlockPos interiorPos = bottomLeft.relative(widthDirection, x).above(height);
                if (!interiorPredicate.test(level.getBlockState(interiorPos))) {
                    if (height < MIN_HEIGHT) {
                        return 0;
                    }
                    return hasTopFrame(level, bottomLeft, widthDirection, width, height, frameTag) ? height : 0;
                }
            }
        }

        return hasTopFrame(level, bottomLeft, widthDirection, width, MAX_SIZE, frameTag) ? MAX_SIZE : 0;
    }

    private static boolean hasTopFrame(
            BlockGetter level,
            BlockPos bottomLeft,
            Direction widthDirection,
            int width,
            int topY,
            TagKey<Block> frameTag
    ) {
        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(bottomLeft.relative(widthDirection, x).above(topY)).is(frameTag)) {
                return false;
            }
        }
        return true;
    }

    public void fill(ServerLevel level) {
        BlockState portalState = this.portalBlock.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, this.axis);
        this.forEachInterior(pos -> level.setBlock(pos, portalState, Block.UPDATE_ALL));
    }

    @Nullable
    public static PermanentPortalShape create(ServerLevel level, BlockPos bottomLeft, PermanentPortalType type, Direction.Axis axis) {
        if (axis == Direction.Axis.Y || !canCreate(level, bottomLeft, type, axis)) {
            return null;
        }

        BlockState frameState = type.platformBlock().defaultBlockState();
        BlockState portalState = type.portalBlock().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
        Direction widthDirection = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;

        for (int y = -1; y <= 3; y++) {
            for (int x = -1; x <= 2; x++) {
                BlockPos pos = bottomLeft.relative(widthDirection, x).above(y);
                boolean cornerSpot = (x == -1 || x == 2) && (y == -1 || y == 3);
                if (cornerSpot) {
                    continue;
                }
                if (x == -1 || x == 2 || y == -1 || y == 3) {
                    level.setBlock(pos, frameState, Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, portalState, Block.UPDATE_ALL);
                }
            }
        }

        return new PermanentPortalShape(type.frameTag(), type.portalBlock(), axis, bottomLeft.immutable(), 2, 3);
    }

    private static boolean canCreate(BlockGetter level, BlockPos bottomLeft, PermanentPortalType type, Direction.Axis axis) {
        Direction widthDirection = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Block frameBlock = type.platformBlock();
        for (int y = -1; y <= 3; y++) {
            for (int x = -1; x <= 2; x++) {
                BlockPos pos = bottomLeft.relative(widthDirection, x).above(y);
                BlockState state = level.getBlockState(pos);
                boolean frameSpot = x == -1 || x == 2 || y == -1 || y == 3;
                boolean cornerSpot = (x == -1 || x == 2) && (y == -1 || y == 3);
                if (cornerSpot) {
                    continue;
                }
                if (frameSpot) {
                    if (!state.isAir() && !state.is(frameBlock) && !state.canBeReplaced()) {
                        return false;
                    }
                } else if (!state.isAir() && !state.canBeReplaced() && !state.is(Blocks.AIR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean contains(BlockPos pos) {
        if (pos.getY() < this.bottomLeft.getY() || pos.getY() >= this.bottomLeft.getY() + this.height) {
            return false;
        }

        if (this.axis == Direction.Axis.X) {
            return pos.getZ() == this.bottomLeft.getZ()
                    && pos.getX() >= this.bottomLeft.getX()
                    && pos.getX() < this.bottomLeft.getX() + this.width;
        }

        return pos.getX() == this.bottomLeft.getX()
                && pos.getZ() >= this.bottomLeft.getZ()
                && pos.getZ() < this.bottomLeft.getZ() + this.width;
    }

    public boolean intersects(AABB box) {
        final boolean[] intersects = {false};
        this.forEachInterior(pos -> {
            if (box.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D)) {
                intersects[0] = true;
            }
        });
        return intersects[0];
    }

    public void forEachInterior(Consumer<BlockPos> consumer) {
        Direction widthDirection = this.axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                consumer.accept(this.bottomLeft.relative(widthDirection, x).above(y));
            }
        }
    }

    public Vec3 center() {
        if (this.axis == Direction.Axis.X) {
            return new Vec3(this.bottomLeft.getX() + (this.width / 2.0D), this.bottomLeft.getY() + (this.height / 2.0D), this.bottomLeft.getZ() + 0.5D);
        }
        return new Vec3(this.bottomLeft.getX() + 0.5D, this.bottomLeft.getY() + (this.height / 2.0D), this.bottomLeft.getZ() + (this.width / 2.0D));
    }
}
