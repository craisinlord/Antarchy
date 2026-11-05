package com.craisinlord.antarchy.content.throwback;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Optional;
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
    private static final int MIN_POLE_HEIGHT = 8;
    private static final int MAX_POLE_HEIGHT = 20;
    private static final int VERTICAL_RANGE = 48;
    private static final long REGION_SALT = 0x7B4C2A19E3D5F061L;
    public static final int APPROACH_DISTANCE = 24;
    private static final int OTHER_STRUCTURE_MARGIN = 8;

    private ThrowbackZones() {
    }

    public record WildZone(int x, int z, int radius, int poleHeight) {
    }

    public record Influence(float strength, boolean inside) {
        public static final Influence NONE = new Influence(0.0F, false);

        public Influence merge(Influence other) {
            return new Influence(Math.max(this.strength, other.strength), this.inside || other.inside);
        }
    }

    public static Optional<WildZone> wildZoneInRegion(long seed, int regionX, int regionZ) {
        RandomSource random = RandomSource.create(seed ^ (regionX * 341873128712L) ^ (regionZ * 132897987541L) ^ REGION_SALT);
        if (random.nextDouble() >= AntarchySettings.throwbackWildZoneChance()) {
            return Optional.empty();
        }
        int span = REGION_SIZE - REGION_MARGIN * 2;
        int x = regionX * REGION_SIZE + REGION_MARGIN + random.nextInt(span);
        int z = regionZ * REGION_SIZE + REGION_MARGIN + random.nextInt(span);
        int minRadius = Math.max(1, AntarchySettings.throwbackWildZoneMinRadius());
        int maxRadius = Math.max(minRadius, AntarchySettings.throwbackWildZoneMaxRadius());
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
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

    public static Influence torchInfluence(BlockPos torch, int radius, BlockPos pos) {
        if (Math.abs(pos.getY() - torch.getY()) > VERTICAL_RANGE) {
            return Influence.NONE;
        }
        int reach = radius + APPROACH_DISTANCE;
        int dx = pos.getX() - torch.getX();
        int dz = pos.getZ() - torch.getZ();
        float distance = Mth.sqrt(dx * dx + dz * dz);
        if (distance > reach) {
            return Influence.NONE;
        }
        return new Influence(strengthFor(distance, radius / 2.0F, reach), distance <= radius);
    }

    public static boolean isInsideOtherStructure(ServerLevel level, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        if (!level.hasChunk(chunk.x, chunk.z)) {
            return false;
        }
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (StructureStart start : level.structureManager().startsForStructure(
                chunk,
                structure -> !registry.wrapAsHolder(structure).is(STRUCTURE_ZONES))) {
            if (start.getBoundingBox().inflatedBy(OTHER_STRUCTURE_MARGIN).isInside(pos)) {
                return true;
            }
        }
        return false;
    }

    private static float strengthFor(float distance, float core, float reach) {
        float linear = Mth.clamp(1.0F - (distance - core) / (reach - core), 0.0F, 1.0F);
        return linear * linear * (3.0F - 2.0F * linear);
    }
}
