package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.content.worldgen.elythia.ElythiaTerrainMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;

public final class ElythiaCommand {
    private static final int DEFAULT_MAP_RADIUS = 512;
    private static final int DEFAULT_MAP_STEP = 4;
    private static final int MAX_MAP_SIZE = 1025;

    private ElythiaCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("elythia")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("map")
                                .executes(context -> map(context, DEFAULT_MAP_RADIUS, DEFAULT_MAP_STEP, false))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(16, 16384))
                                        .executes(context -> map(context, IntegerArgumentType.getInteger(context, "radius"), DEFAULT_MAP_STEP, false))
                                        .then(Commands.argument("step", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> map(context,
                                                        IntegerArgumentType.getInteger(context, "radius"),
                                                        IntegerArgumentType.getInteger(context, "step"), false))
                                                .then(Commands.literal("exact")
                                                        .executes(context -> map(context,
                                                                IntegerArgumentType.getInteger(context, "radius"),
                                                                IntegerArgumentType.getInteger(context, "step"), true))))))
        );
    }

    private static int map(CommandContext<CommandSourceStack> context, int radius, int step, boolean exact) {
        CommandSourceStack source = context.getSource();
        int size = radius * 2 / step + 1;
        if (size > MAX_MAP_SIZE) {
            source.sendFailure(Component.literal("Map would be " + size + "x" + size + " pixels; raise the step or lower the radius (max " + MAX_MAP_SIZE + ")"));
            return 0;
        }
        ServerLevel level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());
        source.sendSuccess(() -> Component.literal("Sampling " + size * size + " columns around " + center.getX() + ", " + center.getZ() + "..."), false);
        CompletableFuture.supplyAsync(() -> {
            try {
                return ElythiaTerrainMap.render(level, center, radius, step, exact);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }, Util.backgroundExecutor()).whenComplete((result, error) -> level.getServer().execute(() -> {
            if (error != null) {
                source.sendFailure(Component.literal("Map failed: " + error.getMessage()));
                return;
            }
            report(source, result);
        }));
        return 1;
    }

    private static void report(CommandSourceStack source, ElythiaTerrainMap.Result result) {
        double microsPerColumn = result.elapsedMillis() * 1000.0D / result.columns();
        String biomes = result.biomeCounts().stream()
                .limit(8)
                .map(entry -> shortName(entry) + " " + percent(entry.getValue() / (double) result.columns()))
                .collect(Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal(result.columns() + " columns in " + result.elapsedMillis() + " ms ("
                + String.format(Locale.ROOT, "%.1f", microsPerColumn) + " us/column)"), false);
        source.sendSuccess(() -> Component.literal("Land height min " + result.minHeight() + ", avg "
                + String.format(Locale.ROOT, "%.1f", result.averageHeight()) + ", max " + result.maxHeight()
                + " | fluid " + percent(result.fluidShare()) + " (lumen " + percent(result.lumenShare()) + ")"), false);
        source.sendSuccess(() -> Component.literal("Biomes: " + biomes), false);
        source.sendSuccess(() -> fileLink(result.heightImage()), false);
        source.sendSuccess(() -> fileLink(result.biomeImage()), false);
    }

    private static Component fileLink(Path path) {
        String file = path.toString();
        return Component.literal(file).withStyle(style -> style
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenFile(path)));
    }

    private static String shortName(Map.Entry<String, Integer> entry) {
        String id = entry.getKey();
        return id.substring(id.indexOf(':') + 1);
    }

    private static String percent(double share) {
        return String.format(Locale.ROOT, "%.1f%%", share * 100.0D);
    }
}
