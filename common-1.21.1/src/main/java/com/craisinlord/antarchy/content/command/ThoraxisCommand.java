package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.weather.ThoraxisEvent;
import com.craisinlord.antarchy.content.weather.ThoraxisEventMath;
import com.craisinlord.antarchy.content.weather.ThoraxisEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class ThoraxisCommand {
    private ThoraxisCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("thoraxis")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("event")
                                .executes(ThoraxisCommand::status)
                                .then(Commands.literal("stop").executes(ThoraxisCommand::stop))
                                .then(Commands.literal("start")
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(ThoraxisEvent.values())
                                                                .filter(event -> event != ThoraxisEvent.NONE)
                                                                .map(ThoraxisEvent::id), builder))
                                                .executes(context -> start(context, false))
                                                .then(Commands.literal("now").executes(context -> start(context, true))))))
        );
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        ServerLevel level = thoraxisLevel(context);
        if (level == null) {
            return 0;
        }
        ThoraxisEvents events = ThoraxisEvents.get(level);
        String detail = events.event() == ThoraxisEvent.NONE ? ""
                : " (" + events.phase().name().toLowerCase(java.util.Locale.ROOT) + ", " + events.remainingTicks(level) / 20L + "s left)";
        context.getSource().sendSuccess(() -> Component.literal("Current Thoraxis event: " + events.event().id() + detail), false);
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> context, boolean skipWarning) {
        ServerLevel level = thoraxisLevel(context);
        if (level == null) {
            return 0;
        }
        String id = StringArgumentType.getString(context, "type");
        ThoraxisEvent event = ThoraxisEvent.byId(id);
        if (event == ThoraxisEvent.NONE) {
            context.getSource().sendFailure(Component.literal("Unknown Thoraxis event: " + id));
            return 0;
        }
        ThoraxisEvents.get(level).start(level, event, skipWarning);
        context.getSource().sendSuccess(() -> Component.literal("Started Thoraxis event: " + event.id()), true);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        ServerLevel level = thoraxisLevel(context);
        if (level == null) {
            return 0;
        }
        ThoraxisEvents.get(level).stop(level);
        context.getSource().sendSuccess(() -> Component.literal("Stopped the current Thoraxis event"), true);
        return 1;
    }

    private static ServerLevel thoraxisLevel(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        if (!ThoraxisEventMath.isThoraxis(level)) {
            context.getSource().sendFailure(Component.literal("Thoraxis events can only be controlled from inside Thoraxis"));
            return null;
        }
        return level;
    }
}
