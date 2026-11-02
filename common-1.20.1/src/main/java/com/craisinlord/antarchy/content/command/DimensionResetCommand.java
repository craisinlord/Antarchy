package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.migration.DimensionResetManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class DimensionResetCommand {
    private DimensionResetCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> toggle = Commands.literal("toggle");
        for (String dimension : DimensionResetManager.DIMENSIONS) {
            toggle.then(Commands.literal(dimension).executes(context -> DimensionResetManager.toggle(context.getSource(), dimension)));
        }
        dispatcher.register(
                Commands.literal(DimensionResetManager.COMMAND)
                        .requires(DimensionResetManager::canManage)
                        .executes(context -> DimensionResetManager.status(context.getSource()))
                        .then(Commands.literal("status").executes(context -> DimensionResetManager.status(context.getSource())))
                        .then(toggle)
                        .then(Commands.literal("confirm").executes(context -> DimensionResetManager.confirm(context.getSource())))
                        .then(Commands.literal("cancel").executes(context -> DimensionResetManager.cancel(context.getSource())))
                        .then(Commands.literal("dismiss").executes(context -> DimensionResetManager.dismiss(context.getSource())))
        );
    }
}
