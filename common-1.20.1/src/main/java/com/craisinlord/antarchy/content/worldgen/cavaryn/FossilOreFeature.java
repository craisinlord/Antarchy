package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class FossilOreFeature extends OreFeature {
    public FossilOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        if (!AntarchySettings.fossilsEnabled()) {
            return false;
        }
        return super.place(context);
    }
}
