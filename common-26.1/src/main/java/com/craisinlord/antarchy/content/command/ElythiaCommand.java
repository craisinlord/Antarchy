package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.craisinlord.antarchy.content.weather.ElythiaEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class ElythiaCommand {
    private ElythiaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("elythia")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("event")
                                .executes(ElythiaCommand::eventStatus)
                                .then(Commands.literal("stop").executes(ElythiaCommand::eventStop))
                                .then(Commands.literal("start")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(ElythiaEvent.values())
                                                                .filter(event -> event != ElythiaEvent.NONE)
                                                                .map(ElythiaEvent::id), builder))
                                                .executes(ElythiaCommand::eventStart))))
        );
    }

    private static int eventStatus(CommandContext<CommandSourceStack> context) {
        ServerLevel level = elythiaLevel(context);
        if (level == null) {
            return 0;
        }
        ElythiaEvents events = ElythiaEvents.get(level);
        long remaining = events.remainingTicks(level);
        String detail = events.event() == ElythiaEvent.NONE ? ""
                : remaining < 0L ? " (until the rain stops)" : " (" + remaining / 20L + "s left)";
        context.getSource().sendSuccess(() -> Component.literal("Current Elythia event: " + events.event().id() + detail), false);
        return 1;
    }

    private static int eventStart(CommandContext<CommandSourceStack> context) {
        ServerLevel level = elythiaLevel(context);
        if (level == null) {
            return 0;
        }
        String id = StringArgumentType.getString(context, "type");
        ElythiaEvent event = ElythiaEvent.byId(id);
        if (event == ElythiaEvent.NONE) {
            context.getSource().sendFailure(Component.literal("Unknown Elythia event: " + id));
            return 0;
        }
        ElythiaEvents.get(level).start(level, event, true);
        context.getSource().sendSuccess(() -> Component.literal("Started Elythia event: " + event.id()), true);
        return 1;
    }

    private static int eventStop(CommandContext<CommandSourceStack> context) {
        ServerLevel level = elythiaLevel(context);
        if (level == null) {
            return 0;
        }
        ElythiaEvents.get(level).stop();
        context.getSource().sendSuccess(() -> Component.literal("Stopped the current Elythia event"), true);
        return 1;
    }

    private static ServerLevel elythiaLevel(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        if (!ElythiaEvents.isElythia(level)) {
            context.getSource().sendFailure(Component.literal("Elythia events can only be controlled from inside Elythia"));
            return null;
        }
        return level;
    }

}
