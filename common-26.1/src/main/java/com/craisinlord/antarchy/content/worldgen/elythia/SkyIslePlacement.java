package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public final class SkyIslePlacement extends RandomSpreadStructurePlacement {
    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "sky_isle_crown");
    private static final int SPACING = SkyIsleGrid.CELL_SIZE >> 4;
    private static final Codec<SkyIsleGrid.Theme> THEME_CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(SkyIsleGrid.Theme.valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> "Unknown sky isle theme: " + name);
                }
            },
            theme -> theme.name().toLowerCase(Locale.ROOT));
    public static final MapCodec<SkyIslePlacement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            placementCodec(instance)
                    .and(THEME_CODEC.fieldOf("theme").forGetter(SkyIslePlacement::theme))
                    .apply(instance, SkyIslePlacement::new));

    private final SkyIsleGrid.Theme theme;

    public SkyIslePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt,
                            Optional<ExclusionZone> exclusionZone, SkyIsleGrid.Theme theme) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, SPACING, 0, RandomSpreadType.LINEAR);
        this.theme = theme;
    }

    public SkyIsleGrid.Theme theme() {
        return this.theme;
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int x, int z) {
        SkyIsleGrid.Island crown = SkyIsleGrid.forSeed(seed).crownInCellOfChunk(x, z);
        if (crown == null) {
            return new ChunkPos(SkyIsleGrid.cellOfChunk(x) * SPACING, SkyIsleGrid.cellOfChunk(z) * SPACING);
        }
        return new ChunkPos(Mth.floor(crown.centerX) >> 4, Mth.floor(crown.centerZ) >> 4);
    }

    @Override
    public boolean applyAdditionalChunkRestrictions(int x, int z, long seed) {
        SkyIsleGrid.Island crown = SkyIsleGrid.forSeed(seed).crownInCellOfChunk(x, z);
        if (crown == null || crown.theme != this.theme) {
            return false;
        }
        if (Mth.floor(crown.centerX) >> 4 != x || Mth.floor(crown.centerZ) >> 4 != z) {
            return false;
        }
        return super.applyAdditionalChunkRestrictions(x, z, seed);
    }

    @Override
    public StructurePlacementType<?> type() {
        return BuiltInRegistries.STRUCTURE_PLACEMENT.getValue(TYPE_ID);
    }
}
