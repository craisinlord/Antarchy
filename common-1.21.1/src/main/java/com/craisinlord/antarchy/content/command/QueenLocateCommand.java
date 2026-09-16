package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import com.craisinlord.antarchy.content.worldgen.thoraxis.QueenTrailGrid;
import com.craisinlord.antarchy.content.entity.royal.QueenEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class QueenLocateCommand {
    private static final ResourceKey<Level> THORAXIS = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis")
    );
    private static final int SEARCH_RADIUS_CELLS = 8;
    private static final int MAX_SITE_CHECKS = 64;

    private QueenLocateCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("locatequeen")
                .requires(source -> source.hasPermission(2))
                .executes(QueenLocateCommand::locate));
    }

    private static int locate(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel thoraxis = source.getServer().getLevel(THORAXIS);
        if (thoraxis == null) {
            source.sendFailure(Component.literal("Thoraxis is not available."));
            return 0;
        }

        int originX = Mth.floor(source.getPosition().x);
        int originZ = Mth.floor(source.getPosition().z);
        int cellX = Math.floorDiv(originX, QueenTrailGrid.SPACING);
        int cellZ = Math.floorDiv(originZ, QueenTrailGrid.SPACING);
        List<QueenTrailGrid.Site> sites = new ArrayList<>();
        for (int x = cellX - SEARCH_RADIUS_CELLS; x <= cellX + SEARCH_RADIUS_CELLS; x++) {
            for (int z = cellZ - SEARCH_RADIUS_CELLS; z <= cellZ + SEARCH_RADIUS_CELLS; z++) {
                sites.add(QueenTrailGrid.site(thoraxis.getSeed(), x, z));
            }
        }
        sites.sort(Comparator.comparingDouble(site -> distanceSquared(originX, originZ, site)));

        int checks = Math.min(MAX_SITE_CHECKS, sites.size());
        for (int index = 0; index < checks; index++) {
            QueenTrailGrid.Site site = sites.get(index);
            thoraxis.getChunk(site.terminalX() >> 4, site.terminalZ() >> 4);
            QueenTrailSpawnMarkerBlockEntity marker = findMarker(thoraxis, site);
            if (marker == null) {
                continue;
            }
            QueenEntity queen = marker.spawnNow(thoraxis);
            BlockPos spawn = marker.getSpawnPos();
            if (queen != null) {
                spawn = BlockPos.containing(queen.position());
            }
            int distance = Mth.floor(Math.sqrt(distanceSquared(originX, originZ, site)));
            String coordinates = "[" + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ() + "]";
            String teleportCommand = "/execute in " + THORAXIS.location() + " run tp @s "
                    + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ();
            source.sendSuccess(() -> Component.literal("Nearest Queen spawn in " + THORAXIS.location() + ": ")
                    .append(Component.literal(coordinates)
                            .withStyle(Style.EMPTY
                                    .withColor(net.minecraft.ChatFormatting.AQUA)
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, teleportCommand))))
                    .append(Component.literal(" (" + distance + " blocks away)")), false);
            return distance;
        }

        source.sendFailure(Component.literal("No configured Queen spawn was found within the search area."));
        return 0;
    }

    private static QueenTrailSpawnMarkerBlockEntity findMarker(ServerLevel level, QueenTrailGrid.Site site) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(site.terminalX(), level.getMinBuildHeight(), site.terminalZ());
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            cursor.setY(y);
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            if (blockEntity instanceof QueenTrailSpawnMarkerBlockEntity marker
                    && marker.isConfigured()
                    && marker.getSiteId() == site.id()) {
                return marker;
            }
        }
        return null;
    }

    private static double distanceSquared(int originX, int originZ, QueenTrailGrid.Site site) {
        double dx = site.terminalX() - originX;
        double dz = site.terminalZ() - originZ;
        return dx * dx + dz * dz;
    }
}
