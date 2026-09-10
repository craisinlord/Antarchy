package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.entity.royal.KingEntity;
import com.craisinlord.antarchy.content.entity.royal.RoyalPunishmentType;
import com.craisinlord.antarchy.content.entity.royal.decree.RoyalDecree;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public final class RoyalCommand {
    private RoyalCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("royal")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("decree")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(RoyalCommand::activateDecree)))
                .then(Commands.literal("punishment")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(RoyalCommand::triggerPunishment))));
    }

    private static int activateDecree(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        KingEntity king = nearestKing(player);
        if (king == null) {
            context.getSource().sendFailure(Component.literal("No King found nearby."));
            return 0;
        }
        String id = StringArgumentType.getString(context, "id");
        if (!king.debugActivateDecree(id, player)) {
            context.getSource().sendFailure(Component.literal("Unknown decree: " + id));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("Activated decree " + id), true);
        return 1;
    }

    private static int triggerPunishment(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        KingEntity king = nearestKing(player);
        if (king == null) {
            context.getSource().sendFailure(Component.literal("No King found nearby."));
            return 0;
        }
        String id = StringArgumentType.getString(context, "id");
        RoyalPunishmentType type = RoyalPunishmentType.fromId(id);
        if (type == null || !king.debugTriggerPunishment(type, player)) {
            context.getSource().sendFailure(Component.literal("Unknown or unavailable punishment: " + id));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal("Triggered punishment " + type.id()), true);
        return 1;
    }

    private static KingEntity nearestKing(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(128.0D);
        return player.serverLevel().getEntitiesOfClass(KingEntity.class, area, LivingEntity::isAlive).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(player), right.distanceToSqr(player)))
                .orElse(null);
    }
}
