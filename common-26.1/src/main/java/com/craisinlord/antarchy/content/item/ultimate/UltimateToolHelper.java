package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.item.GiantFryingPanToolHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class UltimateToolHelper {
    private static final String THREE_BY_THREE_TAG = "antarchy.ultimate_three_by_three";
    private static final String MINING_MODE_TAG = "antarchy.ultimate_mining_mode";
    private static final ThreadLocal<Boolean> BREAKING_EXTRA_BLOCKS = ThreadLocal.withInitial(() -> false);
    private static final float GENERAL_MINING_MULTIPLIER = 1.1F;
    private static final float ULTIMATE_SHOVEL_MINING_SPEED = 24.0F;

    private UltimateToolHelper() {
    }

    public static InteractionResult handleToggleUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || availableModes(toolKindOf(stack.getItem())).size() < 2) {
            return InteractionResult.PASS;
        }

        cycleMiningMode(level, player, stack);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handleToggleUseOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || availableModes(toolKindOf(context.getItemInHand().getItem())).size() < 2) {
            return InteractionResult.PASS;
        }

        cycleMiningMode(context.getLevel(), player, context.getItemInHand());
        return InteractionResult.SUCCESS;
    }

    public static boolean isThreeByThreeEnabled(ItemStack stack) {
        return getMiningMode(stack) == MiningMode.THREE_BY_THREE;
    }

    public static boolean isVeinEnabled(ItemStack stack) {
        return getMiningMode(stack) == MiningMode.VEIN;
    }

    public static MiningMode getMiningMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(MINING_MODE_TAG)) {
            int index = tag.getByteOr(MINING_MODE_TAG, (byte) 0);
            MiningMode[] modes = MiningMode.values();
            return index >= 0 && index < modes.length ? modes[index] : MiningMode.DEFAULT;
        }
        return tag.getBooleanOr(THREE_BY_THREE_TAG, false) ? MiningMode.THREE_BY_THREE : MiningMode.DEFAULT;
    }

    public static float getDestroySpeed(ToolKind toolKind, ToolMaterial material, ItemStack stack, BlockState state, float defaultSpeed) {
        if (matchesToolKind(toolKind, state)) {
            float base = Math.max(defaultSpeed, material.speed());
            if (toolKind == ToolKind.SHOVEL) {
                return Math.max(base * GENERAL_MINING_MULTIPLIER, ULTIMATE_SHOVEL_MINING_SPEED);
            }
            return base * GENERAL_MINING_MULTIPLIER;
        }
        if (defaultSpeed <= 1.0F) {
            return defaultSpeed;
        }
        return defaultSpeed * GENERAL_MINING_MULTIPLIER;
    }

    public static void appendModeTooltip(ToolKind toolKind, Consumer<Component> tooltip) {
        List<MiningMode> modes = availableModes(toolKind);
        boolean area = modes.contains(MiningMode.THREE_BY_THREE);
        boolean vein = modes.contains(MiningMode.VEIN);
        String key;
        if (area && vein) {
            key = "tooltip.antarchy.ultimate_tools_3x3_vein";
        } else if (area) {
            key = "tooltip.antarchy.ultimate_tools_3x3";
        } else if (vein) {
            key = "tooltip.antarchy.ultimate_tools_vein";
        } else {
            return;
        }
        tooltip.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }

    public static void handleAreaMining(ToolKind toolKind, ItemStack stack, Level level, BlockState originState, BlockPos origin, LivingEntity entity) {
        if (level.isClientSide()
                || BREAKING_EXTRA_BLOCKS.get()
                || !(entity instanceof ServerPlayer player)) {
            return;
        }

        MiningMode mode = getMiningMode(stack);
        if (mode == MiningMode.DEFAULT || !availableModes(toolKind).contains(mode)) {
            return;
        }
        if (mode == MiningMode.THREE_BY_THREE) {
            mineArea(toolKind, stack, level, originState, origin, player, 1);
        } else {
            mineVein(toolKind, stack, level, originState, origin, player, veinLimit(originState));
        }
    }

    public static int veinLimit(BlockState state) {
        return state.is(BlockTags.LOGS)
                ? AntarchySettings.ultimateToolsTreeFellingMaxLogs()
                : AntarchySettings.ultimateToolsVeinMiningMaxBlocks();
    }

    public static void mineVein(ToolKind toolKind, ItemStack stack, Level level, BlockState originState, BlockPos origin, ServerPlayer player, int maxBlocks) {
        if (level.isClientSide() || BREAKING_EXTRA_BLOCKS.get() || maxBlocks <= 0 || !matchesToolKind(toolKind, originState)) {
            return;
        }

        breakAll(toolKind, stack, level, player, collectConnected(level, origin, originState.getBlock(), maxBlocks, false));
    }

    public static void mineTree(ItemStack stack, Level level, BlockState originState, BlockPos origin, ServerPlayer player, int maxBlocks) {
        if (level.isClientSide() || BREAKING_EXTRA_BLOCKS.get() || maxBlocks <= 0 || !originState.is(BlockTags.LOGS)) {
            return;
        }

        breakAll(ToolKind.AXE, stack, level, player, collectConnected(level, origin, originState.getBlock(), maxBlocks, true));
    }

    private static void breakAll(ToolKind toolKind, ItemStack stack, Level level, ServerPlayer player, List<BlockPos> targets) {
        if (targets.isEmpty()) {
            return;
        }

        BREAKING_EXTRA_BLOCKS.set(true);
        try {
            for (BlockPos targetPos : targets) {
                if (!canBreak(toolKind, player, stack, level, targetPos, level.getBlockState(targetPos))) {
                    continue;
                }

                player.gameMode.destroyBlock(targetPos);
                if (stack.isEmpty()) {
                    break;
                }
            }
        } finally {
            BREAKING_EXTRA_BLOCKS.set(false);
        }
    }

    private static List<BlockPos> collectConnected(Level level, BlockPos origin, Block block, int maxBlocks, boolean requireFoliage) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);
        boolean foliage = false;

        while (!queue.isEmpty() && found.size() < maxBlocks) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1 && found.size() < maxBlocks; dx++) {
                for (int dy = -1; dy <= 1 && found.size() < maxBlocks; dy++) {
                    for (int dz = -1; dz <= 1 && found.size() < maxBlocks; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockPos next = current.offset(dx, dy, dz);
                        if (!visited.add(next) || !level.isLoaded(next)) {
                            continue;
                        }

                        BlockState state = level.getBlockState(next);
                        if (state.is(block)) {
                            found.add(next);
                            queue.add(next);
                        } else if (requireFoliage && !foliage && isNaturalFoliage(state)) {
                            foliage = true;
                        }
                    }
                }
            }
        }

        return requireFoliage && !foliage ? List.of() : found;
    }

    private static boolean isNaturalFoliage(BlockState state) {
        if (state.is(BlockTags.LEAVES)) {
            return !state.hasProperty(BlockStateProperties.PERSISTENT) || !state.getValue(BlockStateProperties.PERSISTENT);
        }
        return state.is(BlockTags.WART_BLOCKS) || state.is(Blocks.SHROOMLIGHT);
    }

    public static void mineArea(ToolKind toolKind, ItemStack stack, Level level, BlockState originState, BlockPos origin, ServerPlayer player, int radius) {
        if (level.isClientSide() || BREAKING_EXTRA_BLOCKS.get() || !canBreak(toolKind, player, stack, level, origin, originState)) {
            return;
        }

        Direction face = resolveMiningFace(player, origin);
        BREAKING_EXTRA_BLOCKS.set(true);
        try {
            for (BlockPos targetPos : getPlane(origin, face, radius)) {
                if (!canBreak(toolKind, player, stack, level, targetPos, level.getBlockState(targetPos))) {
                    continue;
                }

                player.gameMode.destroyBlock(targetPos);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.destroyBlockProgress(syntheticBreakerId(player.getId(), targetPos), targetPos, -1);
                }
                if (stack.isEmpty()) {
                    break;
                }
            }
        } finally {
            BREAKING_EXTRA_BLOCKS.set(false);
        }
    }

    public static void broadcastAreaMiningProgress(ServerPlayer player, ServerLevel level, BlockPos origin, int stage) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GiantFryingPanItem) {
            broadcastPlaneProgress(ToolKind.SHOVEL, player, level, origin, stage, GiantFryingPanToolHelper.MINING_RADIUS);
            return;
        }

        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled()) {
            return;
        }

        if (stack.isEmpty() || !isThreeByThreeEnabled(stack)) {
            return;
        }

        ToolKind kind = toolKindOf(stack.getItem());
        if (kind == null) {
            return;
        }

        broadcastPlaneProgress(kind, player, level, origin, stage, 1);
    }

    private static void broadcastPlaneProgress(ToolKind kind, ServerPlayer player, ServerLevel level, BlockPos origin, int stage, int radius) {
        BlockState originState = level.getBlockState(origin);
        if (!matchesToolKind(kind, originState)) {
            return;
        }

        Direction face = resolveMiningFace(player, origin);
        for (BlockPos targetPos : getPlane(origin, face, radius)) {
            BlockState state = level.getBlockState(targetPos);
            if (state.isAir() || state.hasBlockEntity() || state.getDestroySpeed(level, targetPos) < 0.0F) {
                continue;
            }
            if (!matchesToolKind(kind, state)) {
                continue;
            }
            level.destroyBlockProgress(syntheticBreakerId(player.getId(), targetPos), targetPos, stage);
        }
    }

    private static int syntheticBreakerId(int playerId, BlockPos pos) {
        int hash = pos.hashCode() ^ (playerId * 8191);
        return hash | 0x80000000;
    }

    private static ToolKind toolKindOf(Item item) {
        if (item instanceof UltimatePickaxeItem) return ToolKind.PICKAXE;
        if (item instanceof UltimateAxeItem) return ToolKind.AXE;
        if (item instanceof UtlimateShovelItem) return ToolKind.SHOVEL;
        if (item instanceof UltimateHoeItem) return ToolKind.HOE;
        return null;
    }

    public static void handleAreaUseOn(UseOnContext context, Function<UseOnContext, InteractionResult> action) {
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled()
                || context.getLevel().isClientSide()
                || !isThreeByThreeEnabled(context.getItemInHand())) {
            return;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        for (BlockPos targetPos : getPlane(context.getClickedPos(), context.getClickedFace(), 1)) {
            if (!canModify(player, context.getLevel(), targetPos, context.getClickedFace(), context.getItemInHand())) {
                continue;
            }

            UseOnContext areaContext = new UseOnContext(
                    player,
                    context.getHand(),
                    new BlockHitResult(Vec3.atCenterOf(targetPos), context.getClickedFace(), targetPos, context.isInside())
            );
            action.apply(areaContext);
            if (context.getItemInHand().isEmpty()) {
                break;
            }
        }
    }

    public enum ToolKind {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE
    }

    public enum MiningMode {
        DEFAULT,
        THREE_BY_THREE,
        VEIN
    }

    private static List<MiningMode> availableModes(ToolKind toolKind) {
        List<MiningMode> modes = new ArrayList<>(3);
        if (toolKind == null) {
            return modes;
        }
        modes.add(MiningMode.DEFAULT);
        if (AntarchySettings.ultimateToolsThreeByThreeEnabled()) {
            modes.add(MiningMode.THREE_BY_THREE);
        }
        if (AntarchySettings.ultimateToolsVeinMiningEnabled() && (toolKind == ToolKind.PICKAXE || toolKind == ToolKind.AXE)) {
            modes.add(MiningMode.VEIN);
        }
        return modes;
    }

    private static void cycleMiningMode(Level level, Player player, ItemStack stack) {
        List<MiningMode> modes = availableModes(toolKindOf(stack.getItem()));
        int current = modes.indexOf(getMiningMode(stack));
        MiningMode next = modes.get((current + 1) % modes.size());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(THREE_BY_THREE_TAG);
            if (next == MiningMode.DEFAULT) {
                tag.remove(MINING_MODE_TAG);
            } else {
                tag.putByte(MINING_MODE_TAG, (byte) next.ordinal());
            }
        });

        if (!level.isClientSide()) {
            String key = switch (next) {
                case DEFAULT -> "message.antarchy.ultimate_tool.mode_default";
                case THREE_BY_THREE -> "message.antarchy.ultimate_tool.mode_3x3";
                case VEIN -> "message.antarchy.ultimate_tool.mode_vein";
            };
            player.sendOverlayMessage(Component.translatable(key));
        }
    }

    private static boolean canModify(Player player, Level level, BlockPos pos, Direction face, ItemStack stack) {
        if (!level.isLoaded(pos) || !player.mayUseItemAt(pos, face, stack)) {
            return false;
        }

        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.blockActionRestricted(level, pos, serverPlayer.gameMode.getGameModeForPlayer())) {
            return false;
        }

        return true;
    }

    private static boolean canBreak(ToolKind toolKind, ServerPlayer player, ItemStack stack, Level level, BlockPos pos, BlockState state) {
        if (!level.isLoaded(pos)
                || state.isAir()
                || state.hasBlockEntity()
                || state.getDestroySpeed(level, pos) < 0.0F
                || !player.mayUseItemAt(pos, Direction.UP, stack)
                || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
            return false;
        }

        if (!matchesToolKind(toolKind, state)) {
            return false;
        }

        if (stack.getDestroySpeed(state) <= 1.0F) {
            return false;
        }

        return !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state);
    }

    public static boolean matchesToolKind(ToolKind toolKind, BlockState state) {
        return switch (toolKind) {
            case PICKAXE -> state.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> state.is(BlockTags.MINEABLE_WITH_AXE);
            case SHOVEL -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case HOE -> state.is(BlockTags.MINEABLE_WITH_HOE);
        };
    }

    private static Direction resolveMiningFace(ServerPlayer player, BlockPos pos) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 delta = eye.subtract(blockCenter);

        if (delta.lengthSqr() < 1.0E-4) {
            return player.getDirection().getOpposite();
        }

        Direction best = Direction.UP;
        double bestDot = -Double.MAX_VALUE;
        for (Direction d : Direction.values()) {
            double dot = delta.x * d.getStepX() + delta.y * d.getStepY() + delta.z * d.getStepZ();
            if (dot > bestDot) {
                bestDot = dot;
                best = d;
            }
        }
        return best;
    }

    private static List<BlockPos> getPlane(BlockPos center, Direction face, int radius) {
        int size = radius * 2 + 1;
        List<BlockPos> positions = new ArrayList<>(size * size - 1);
        switch (face.getAxis()) {
            case Y -> {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }

                        positions.add(center.offset(x, 0, z));
                    }
                }
            }
            case X -> {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (y == 0 && z == 0) {
                            continue;
                        }

                        positions.add(center.offset(0, y, z));
                    }
                }
            }
            case Z -> {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        if (x == 0 && y == 0) {
                            continue;
                        }

                        positions.add(center.offset(x, y, 0));
                    }
                }
            }
        }

        return positions;
    }
}
