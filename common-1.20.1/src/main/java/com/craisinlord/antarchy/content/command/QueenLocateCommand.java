package com.craisinlord.antarchy.content.command;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import com.craisinlord.antarchy.content.worldgen.thoraxis.QueenTrailGrid;
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
    private static final ResourceKey<Level> THORAXIS = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(Antarchy.MODID, "thoraxis"));
    private QueenLocateCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("locatequeen").requires(source -> source.hasPermission(2))
                .executes(QueenLocateCommand::locate));
    }

    private static int locate(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getServer().getLevel(THORAXIS);
        if (level == null) { source.sendFailure(Component.literal("Thoraxis is not available.")); return 0; }
        int ox = Mth.floor(source.getPosition().x), oz = Mth.floor(source.getPosition().z);
        int cx = Math.floorDiv(ox, QueenTrailGrid.SPACING), cz = Math.floorDiv(oz, QueenTrailGrid.SPACING);
        List<QueenTrailGrid.Site> sites = new ArrayList<>();
        for (int x = cx - 8; x <= cx + 8; x++) for (int z = cz - 8; z <= cz + 8; z++) sites.add(QueenTrailGrid.site(level.getSeed(), x, z));
        sites.sort(Comparator.comparingDouble(site -> distanceSquared(ox, oz, site)));
        for (int i = 0; i < Math.min(64, sites.size()); i++) {
            QueenTrailGrid.Site site = sites.get(i);
            level.getChunk(site.terminalX() >> 4, site.terminalZ() >> 4);
            QueenTrailSpawnMarkerBlockEntity marker = findMarker(level, site);
            if (marker == null) continue;
            BlockPos spawn = marker.getSpawnPos();
            int distance = Mth.floor(Math.sqrt(distanceSquared(ox, oz, site)));
            String coords = "[" + spawn.getX() + ", " + spawn.getY() + ", " + spawn.getZ() + "]";
            String tp = "/execute in " + THORAXIS.location() + " run tp @s " + spawn.getX() + " " + spawn.getY() + " " + spawn.getZ();
            source.sendSuccess(() -> Component.literal("Nearest Queen spawn in " + THORAXIS.location() + ": ")
                    .append(Component.literal(coords).withStyle(Style.EMPTY.withColor(net.minecraft.ChatFormatting.AQUA)
                            .withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tp))))
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
            BlockEntity entity = level.getBlockEntity(cursor);
            if (entity instanceof QueenTrailSpawnMarkerBlockEntity marker && marker.isConfigured() && marker.getSiteId() == site.id()) return marker;
        }
        return null;
    }
    private static double distanceSquared(int ox, int oz, QueenTrailGrid.Site site) {
        double dx = site.terminalX() - ox, dz = site.terminalZ() - oz;
        return dx * dx + dz * dz;
    }
}
