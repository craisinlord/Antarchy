package com.craisinlord.antarchy.content.glimmerecho;

import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGrid;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface GlimmerSite {
    String STRUCTURE = "structure";
    String BIOME = "biome";
    String KINGS_TREE = "kings_tree";
    String UNDERSIDE = "underside";

    Optional<Match> find(ServerLevel level, BlockPos pos);

    record Match(BoundingBox area, String kind, BlockPos anchor, BlockPos spawnCenter) {
        public boolean is(String siteKind) {
            return this.kind.equals(siteKind);
        }
    }

    static GlimmerSite structure(TagKey<Structure> tag) {
        return new StructureSite(tag);
    }

    static GlimmerSite biome(TagKey<Biome> tag) {
        return new BiomeSite(tag);
    }

    static GlimmerSite kingsTree() {
        return new KingsTreeSite();
    }

    static GlimmerSite underside() {
        return new UndersideSite();
    }

    static GlimmerSite anyOf(GlimmerSite... sites) {
        return new AnySite(List.of(sites));
    }

    private static BoundingBox around(BlockPos pos, int horizontal, int vertical) {
        return new BoundingBox(pos.getX() - horizontal, pos.getY() - vertical, pos.getZ() - horizontal,
                pos.getX() + horizontal, pos.getY() + vertical, pos.getZ() + horizontal);
    }

    record StructureSite(TagKey<Structure> tag) implements GlimmerSite {
        private static final int SITE_MARGIN = 8;

        @Override
        public Optional<Match> find(ServerLevel level, BlockPos pos) {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            for (StructureStart start : level.structureManager().startsForStructure(
                    new ChunkPos(pos),
                    structure -> registry.wrapAsHolder(structure).is(this.tag))) {
                BoundingBox box = start.getBoundingBox();
                if (box.inflatedBy(SITE_MARGIN).isInside(pos)) {
                    return Optional.of(new Match(box, STRUCTURE, box.getCenter(), box.getCenter()));
                }
            }
            return Optional.empty();
        }
    }

    record BiomeSite(TagKey<Biome> tag) implements GlimmerSite {
        private static final int HORIZONTAL = 32;
        private static final int VERTICAL = 24;

        @Override
        public Optional<Match> find(ServerLevel level, BlockPos pos) {
            if (!level.getBiome(pos).is(this.tag)) {
                return Optional.empty();
            }
            return Optional.of(new Match(around(pos, HORIZONTAL, VERTICAL), BIOME, pos, pos));
        }
    }

    record KingsTreeSite() implements GlimmerSite {
        private static final int TREE_RADIUS = 192;

        @Override
        public Optional<Match> find(ServerLevel level, BlockPos pos) {
            if (!level.dimension().equals(KingsTreeGrid.ELYTHIA)) {
                return Optional.empty();
            }
            BlockPos center = KingsTreeGrid.nearestTreeCenter(pos);
            double dx = pos.getX() - center.getX();
            double dz = pos.getZ() - center.getZ();
            if (dx * dx + dz * dz > (double) TREE_RADIUS * TREE_RADIUS) {
                return Optional.empty();
            }
            BoundingBox area = new BoundingBox(center.getX() - TREE_RADIUS, level.getMinBuildHeight(), center.getZ() - TREE_RADIUS,
                    center.getX() + TREE_RADIUS, level.getMaxBuildHeight(), center.getZ() + TREE_RADIUS);
            return Optional.of(new Match(area, KINGS_TREE, center, pos));
        }
    }

    record UndersideSite() implements GlimmerSite {
        private static final int HORIZONTAL = 32;
        private static final int VERTICAL = 24;

        @Override
        public Optional<Match> find(ServerLevel level, BlockPos pos) {
            if (!ThoraxisUndersideManager.isThoraxis(level) || pos.getY() >= 0) {
                return Optional.empty();
            }
            return Optional.of(new Match(around(pos, HORIZONTAL, VERTICAL), UNDERSIDE, pos, pos));
        }
    }

    record AnySite(List<GlimmerSite> sites) implements GlimmerSite {
        @Override
        public Optional<Match> find(ServerLevel level, BlockPos pos) {
            for (GlimmerSite site : this.sites) {
                Optional<Match> match = site.find(level, pos);
                if (match.isPresent()) {
                    return match;
                }
            }
            return Optional.empty();
        }
    }
}
