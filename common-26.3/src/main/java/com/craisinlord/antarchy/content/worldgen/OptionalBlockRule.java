package com.craisinlord.antarchy.content.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;

public final class OptionalBlockRule {
    private OptionalBlockRule() {
    }

    public record Source(Identifier block) implements MaterialRule {
        public static final MapCodec<Source> CODEC = Identifier.CODEC.fieldOf("block").xmap(Source::new, Source::block);

        @Override
        public MapCodec<Source> codec() {
            return CODEC;
        }

        @Override
        public RuleEvaluator compile(MaterialRuleContext context) {
            BlockState resolved = BuiltInRegistries.BLOCK.getOptional(this.block).map(Block::defaultBlockState).orElse(null);
            return (x, y, z) -> resolved;
        }
    }
}
