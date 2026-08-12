package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.entity.CaterpillarEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class CaterpillarCommand {
    private CaterpillarCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("caterpillar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("pupate")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .executes(context -> forcePupation(context, EntityArgument.getEntities(context, "targets")))))
        );
    }

    private static int forcePupation(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets) {
        int affected = 0;
        for (Entity target : targets) {
            if (target instanceof CaterpillarEntity caterpillar) {
                caterpillar.forcePupation();
                affected++;
            }
        }

        final int affectedCount = affected;
        context.getSource().sendSuccess(
                () -> Component.literal("Forced pupation for " + affectedCount + " caterpillar(s)"),
                true
        );
        return affectedCount;
    }
}
