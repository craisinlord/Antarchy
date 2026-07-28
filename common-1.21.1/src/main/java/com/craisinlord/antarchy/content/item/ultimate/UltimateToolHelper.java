package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class UltimateToolHelper {
    private static final String THREE_BY_THREE_TAG = "antarchy.ultimate_three_by_three";
    private static final ThreadLocal<Boolean> BREAKING_EXTRA_BLOCKS = ThreadLocal.withInitial(() -> false);
    private static final float GENERAL_MINING_MULTIPLIER = 1.1F;
    private static final float ULTIMATE_SHOVEL_MINING_SPEED = 24.0F;

    private UltimateToolHelper() {
    }

    public static InteractionResultHolder<ItemStack> handleToggleUse(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled() || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        toggleThreeByThree(level, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static InteractionResult handleToggleUseOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled() || player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        toggleThreeByThree(context.getLevel(), player, context.getItemInHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    public static boolean isThreeByThreeEnabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(THREE_BY_THREE_TAG);
    }

    public static float getDestroySpeed(ToolKind toolKind, Tier tier, ItemStack stack, BlockState state, float defaultSpeed) {
        if (matchesToolKind(toolKind, state)) {
            float base = Math.max(defaultSpeed, tier.getSpeed());
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

    public static void appendThreeByThreeTooltip(List<Component> tooltip) {
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled()) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.antarchy.ultimate_tools_3x3").withStyle(ChatFormatting.GRAY));
    }

    public static void handleAreaMining(ToolKind toolKind, ItemStack stack, Level level, BlockState originState, BlockPos origin, LivingEntity entity) {
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled()
                || level.isClientSide
                || BREAKING_EXTRA_BLOCKS.get()
                || !isThreeByThreeEnabled(stack)
                || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (!canBreak(toolKind, player, stack, level, origin, originState)) {
            return;
        }

        Direction face = resolveMiningFace(player, origin);
        BREAKING_EXTRA_BLOCKS.set(true);
        try {
            for (BlockPos targetPos : getMiningPlane(origin, face)) {
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
        if (!AntarchySettings.ultimateToolsThreeByThreeEnabled()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !isThreeByThreeEnabled(stack)) {
            return;
        }

        ToolKind kind = toolKindOf(stack.getItem());
        if (kind == null) {
            return;
        }

        BlockState originState = level.getBlockState(origin);
        if (!matchesToolKind(kind, originState)) {
            return;
        }

        Direction face = resolveMiningFace(player, origin);
        for (BlockPos targetPos : getPlane(origin, face)) {
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
                || context.getLevel().isClientSide
                || !isThreeByThreeEnabled(context.getItemInHand())) {
            return;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        for (BlockPos targetPos : getPlane(context.getClickedPos(), context.getClickedFace())) {
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

    private static void toggleThreeByThree(Level level, Player player, ItemStack stack) {
        boolean enabled = !isThreeByThreeEnabled(stack);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (enabled) {
                tag.putBoolean(THREE_BY_THREE_TAG, true);
            } else {
                tag.remove(THREE_BY_THREE_TAG);
            }
        });

        if (!level.isClientSide) {
            player.displayClientMessage(
                    Component.translatable(
                            enabled
                                    ? "message.antarchy.ultimate_tool.mode_3x3"
                                    : "message.antarchy.ultimate_tool.mode_default"
                    ),
                    true
            );
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

    private static List<BlockPos> getMiningPlane(BlockPos center, Direction face) {
        return getPlane(center, face);
    }

    private static List<BlockPos> getPlane(BlockPos center, Direction face) {
        List<BlockPos> positions = new ArrayList<>(8);
        switch (face.getAxis()) {
            case Y -> {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && z == 0) {
                            continue;
                        }

                        positions.add(center.offset(x, 0, z));
                    }
                }
            }
            case X -> {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (y == 0 && z == 0) {
                            continue;
                        }

                        positions.add(center.offset(0, y, z));
                    }
                }
            }
            case Z -> {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
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
