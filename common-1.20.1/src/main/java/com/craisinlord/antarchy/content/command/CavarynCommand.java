package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.horde.CavarynHordeManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CavarynCommand {
    private CavarynCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("cavaryn")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("horde")
                                .then(Commands.literal("get")
                                        .executes(context -> getHordeLevel(context, java.util.List.of(context.getSource().getPlayerOrException())))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> getHordeLevel(context, EntityArgument.getPlayers(context, "targets")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0, CavarynHordeManager.maxHordeLevel()))
                                                .executes(context -> setHordeLevel(context, IntegerArgumentType.getInteger(context, "level"), java.util.List.of(context.getSource().getPlayerOrException())))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(context -> setHordeLevel(context, IntegerArgumentType.getInteger(context, "level"), EntityArgument.getPlayers(context, "targets")))))))
        );
    }

    private static int getHordeLevel(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        int found = 0;
        for (ServerPlayer target : targets) {
            int levelValue = CavarynHordeManager.getHordeLevel(target);
            if (levelValue < 0) {
                context.getSource().sendSuccess(
                        () -> Component.literal(target.getName().getString() + ": not in Cavaryn"),
                        false
                );
                continue;
            }

            found++;
            context.getSource().sendSuccess(
                    () -> Component.literal(target.getName().getString() + ": Cavaryn horde level " + levelValue + "/" + CavarynHordeManager.maxHordeLevel()),
                    false
            );
        }
        return found;
    }

    private static int setHordeLevel(CommandContext<CommandSourceStack> context, int levelValue, Collection<ServerPlayer> targets) {
        int changed = 0;
        for (ServerPlayer target : targets) {
            if (CavarynHordeManager.setHordeLevel(target, levelValue) >= 0) {
                changed++;
            }
        }

        int finalChanged = changed;
        context.getSource().sendSuccess(
                () -> Component.literal("Set Cavaryn horde level to " + levelValue + " for " + finalChanged + " player(s) in Cavaryn"),
                true
        );
        return changed;
    }
}
