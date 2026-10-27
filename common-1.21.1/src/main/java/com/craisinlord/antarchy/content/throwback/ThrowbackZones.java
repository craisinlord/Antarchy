package com.craisinlord.antarchy.content.throwback;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class ThrowbackZones {
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    public static final TagKey<Structure> STRUCTURE_ZONES = TagKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "throwback_zones")
    );
    public static final TagKey<Biome> WILD_ZONE_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "has_throwback_zone")
    );

    private static final int REGION_SIZE = 320;
    private static final int REGION_MARGIN = 48;
    private static final int MIN_WILD_RADIUS = 48;
    private static final int MAX_WILD_RADIUS = 72;
    private static final int MIN_POLE_HEIGHT = 8;
    private static final int MAX_POLE_HEIGHT = 20;
    private static final int WILD_VERTICAL_RANGE = 48;
    private static final long REGION_SALT = 0x7B4C2A19E3D5F061L;
    public static final int APPROACH_DISTANCE = 24;

    private ThrowbackZones() {
    }

    public record WildZone(int x, int z, int radius, int poleHeight) {
    }

    public record Influence(float strength, boolean inside) {
        public static final Influence NONE = new Influence(0.0F, false);
    }

    public static Optional<WildZone> wildZoneInRegion(long seed, int regionX, int regionZ) {
        RandomSource random = RandomSource.create(seed ^ (regionX * 341873128712L) ^ (regionZ * 132897987541L) ^ REGION_SALT);
        if (random.nextDouble() >= AntarchySettings.throwbackWildZoneChance()) {
            return Optional.empty();
        }
        int span = REGION_SIZE - REGION_MARGIN * 2;
        int x = regionX * REGION_SIZE + REGION_MARGIN + random.nextInt(span);
        int z = regionZ * REGION_SIZE + REGION_MARGIN + random.nextInt(span);
        int radius = MIN_WILD_RADIUS + random.nextInt(MAX_WILD_RADIUS - MIN_WILD_RADIUS + 1);
        int poleHeight = MIN_POLE_HEIGHT + random.nextInt(MAX_POLE_HEIGHT - MIN_POLE_HEIGHT + 1);
        return Optional.of(new WildZone(x, z, radius, poleHeight));
    }

    public static Optional<WildZone> wildZoneInChunk(long seed, ChunkPos chunk) {
        int regionX = Math.floorDiv(chunk.getMinBlockX(), REGION_SIZE);
        int regionZ = Math.floorDiv(chunk.getMinBlockZ(), REGION_SIZE);
        return wildZoneInRegion(seed, regionX, regionZ)
                .filter(zone -> (zone.x() >> 4) == chunk.x && (zone.z() >> 4) == chunk.z);
    }

    public static boolean isWildZoneBiome(LevelReader level, BlockPos pos) {
        return level.getBiome(pos).is(WILD_ZONE_BIOMES);
    }

    public static Influence influenceAt(ServerLevel level, BlockPos pos) {
        Influence structure = structureInfluence(level, pos);
        Influence wild = wildInfluence(level, pos);
        return new Influence(Math.max(structure.strength(), wild.strength()), structure.inside() || wild.inside());
    }

    private static Influence structureInfluence(ServerLevel level, BlockPos pos) {
        int radius = Math.max(1, AntarchySettings.throwbackStructureZoneRadius());
        int reach = radius + APPROACH_DISTANCE;
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Set<StructureStart> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        LongSet probedChunks = new LongOpenHashSet();
        float strength = 0.0F;
        boolean inside = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunk = new ChunkPos(pos.offset(dx * reach, 0, dz * reach));
                if (!probedChunks.add(chunk.toLong()) || !level.hasChunk(chunk.x, chunk.z)) {
                    continue;
                }
                for (StructureStart start : level.structureManager().startsForStructure(
                        chunk,
                        structure -> registry.wrapAsHolder(structure).is(STRUCTURE_ZONES))) {
                    if (!seen.add(start)) {
                        continue;
                    }
                    float distance = distanceToBox(start.getBoundingBox(), pos);
                    if (distance <= reach) {
                        strength = Math.max(strength, strengthFor(distance, reach));
                        inside |= distance <= radius;
                    }
                }
            }
        }
        return new Influence(strength, inside);
    }

    private static Influence wildInfluence(ServerLevel level, BlockPos pos) {
        int regionX = Math.floorDiv(pos.getX(), REGION_SIZE);
        int regionZ = Math.floorDiv(pos.getZ(), REGION_SIZE);
        float strength = 0.0F;
        boolean inside = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Optional<WildZone> found = wildZoneInRegion(level.getSeed(), regionX + dx, regionZ + dz);
                if (found.isEmpty()) {
                    continue;
                }
                Influence influence = wildZoneInfluence(level, pos, found.get());
                strength = Math.max(strength, influence.strength());
                inside |= influence.inside();
            }
        }
        return new Influence(strength, inside);
    }

    private static Influence wildZoneInfluence(ServerLevel level, BlockPos pos, WildZone zone) {
        int reach = zone.radius() + APPROACH_DISTANCE;
        double dx = pos.getX() + 0.5D - (zone.x() + 0.5D);
        double dz = pos.getZ() + 0.5D - (zone.z() + 0.5D);
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance > reach || !level.hasChunk(zone.x() >> 4, zone.z() >> 4)) {
            return Influence.NONE;
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, zone.x(), zone.z());
        if (Math.abs(pos.getY() - surface) > WILD_VERTICAL_RANGE) {
            return Influence.NONE;
        }
        if (!isWildZoneBiome(level, new BlockPos(zone.x(), surface, zone.z()))) {
            return Influence.NONE;
        }
        return new Influence(strengthFor(distance, reach), distance <= zone.radius());
    }

    private static float distanceToBox(BoundingBox box, BlockPos pos) {
        int dx = Math.max(Math.max(box.minX() - pos.getX(), 0), pos.getX() - box.maxX());
        int dy = Math.max(Math.max(box.minY() - pos.getY(), 0), pos.getY() - box.maxY());
        int dz = Math.max(Math.max(box.minZ() - pos.getZ(), 0), pos.getZ() - box.maxZ());
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float strengthFor(float distance, float reach) {
        float linear = Mth.clamp(1.0F - distance / reach, 0.0F, 1.0F);
        return linear * linear * (3.0F - 2.0F * linear);
    }
}
