package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DimensionalTearCommand {
    private static final double DEFAULT_GAP = 24.0D;
    private static final double VERTICAL_OFFSET = 1.0D;

    private DimensionalTearCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dimensionaltear")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawnPair(context, DEFAULT_GAP))
                        .then(Commands.literal("single")
                                .executes(DimensionalTearCommand::spawnSingle))
                        .then(Commands.argument("blocksAhead", DoubleArgumentType.doubleArg(2.0D, 512.0D))
                                .executes(context -> spawnPair(context, DoubleArgumentType.getDouble(context, "blocksAhead"))))
        );
    }

    private static int lifetime() {
        return Math.max(1200, AntarchySettings.dimensionalTearLifetimeTicks());
    }

    private static Vec3 anchorPos(ServerPlayer player) {
        return player.position().add(0.0D, VERTICAL_OFFSET, 0.0D);
    }

    private static int spawnSingle(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();
        DimensionalTearEntity tear = DimensionalTearEntity.create(level, anchorPos(player), player.getYRot(), lifetime());
        level.addFreshEntity(tear);
        context.getSource().sendSuccess(() -> Component.literal("Spawned an unlinked dimensional tear"), true);
        return 1;
    }

    private static int spawnPair(CommandContext<CommandSourceStack> context, double gap) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getLevel();

        float yawA = player.getYRot();
        Vec3 posA = anchorPos(player);
        Vec3 forward = Vec3.directionFromRotation(0.0F, yawA);
        Vec3 posB = posA.add(forward.scale(gap));
        float yawB = Mth.wrapDegrees(yawA + 180.0F);

        DimensionalTearEntity tearA = DimensionalTearEntity.create(level, posA, yawA, lifetime());
        DimensionalTearEntity tearB = DimensionalTearEntity.create(level, posB, yawB, lifetime());
        tearA.linkTo(tearB);
        tearB.linkTo(tearA);
        level.addFreshEntity(tearA);
        level.addFreshEntity(tearB);

        int spacing = Mth.floor(gap);
        context.getSource().sendSuccess(
                () -> Component.literal("Spawned a linked dimensional tear pair, " + spacing + " blocks apart"),
                true
        );
        return 1;
    }
}
