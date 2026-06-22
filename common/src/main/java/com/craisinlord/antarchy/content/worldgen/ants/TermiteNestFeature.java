package com.craisinlord.antarchy.content.worldgen.ants;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class TermiteNestFeature extends SurfaceAntNestFeature {
    public TermiteNestFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }
}
