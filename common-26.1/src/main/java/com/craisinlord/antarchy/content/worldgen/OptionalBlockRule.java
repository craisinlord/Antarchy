package com.craisinlord.antarchy.content.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class OptionalBlockRule extends SurfaceRules {
    private OptionalBlockRule() {
    }

    public record Source(Identifier block) implements RuleSource {
        public static final MapCodec<Source> CODEC = Identifier.CODEC.fieldOf("block").xmap(Source::new, Source::block);
        public static final KeyDispatchDataCodec<Source> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return KEY_CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            BlockState resolved = BuiltInRegistries.BLOCK.getOptional(this.block).map(Block::defaultBlockState).orElse(null);
            return (x, y, z) -> resolved;
        }
    }
}
