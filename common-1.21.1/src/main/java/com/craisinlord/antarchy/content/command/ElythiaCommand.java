package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.network.ElythiaEchoTestSync;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class ElythiaCommand {
    private ElythiaCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("elythia")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("echo")
                                .then(Commands.literal("play").executes(context -> echo(context, true)))
                                .then(Commands.literal("trigger").executes(context -> echo(context, false))))
        );
    }

    private static int echo(CommandContext<CommandSourceStack> context, boolean immediate) throws CommandSyntaxException {
        ElythiaEchoTestSync.send(context.getSource().getPlayerOrException(), immediate);
        return 1;
    }
}
