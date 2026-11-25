package com.craisinlord.antarchy.content.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;
import org.jspecify.annotations.Nullable;

public final class OptionalBiomeRule {
    private OptionalBiomeRule() {
    }

    public record Source(ResourceKey<Biome> biome, MaterialRule thenRun) implements MaterialRule {
        public static final MapCodec<Source> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(Source::biome),
                MaterialRule.CODEC.fieldOf("then_run").forGetter(Source::thenRun)
        ).apply(instance, Source::new));

        @Override
        public MapCodec<Source> codec() {
            return CODEC;
        }

        @Override
        public RuleEvaluator compile(MaterialRuleContext context) {
            return new Rule(context, this.biome, this.thenRun.compile(context));
        }
    }

    private static final class Rule implements RuleEvaluator {
        private final MaterialRuleContext context;
        private final ResourceKey<Biome> biome;
        private final RuleEvaluator rule;
        @Nullable
        private Holder<Biome> lastBiome;
        private boolean lastMatches;

        private Rule(MaterialRuleContext context, ResourceKey<Biome> biome, RuleEvaluator rule) {
            this.context = context;
            this.biome = biome;
            this.rule = rule;
        }

        @Override
        public BlockState tryApply(int blockX, int blockY, int blockZ) {
            Holder<Biome> current = this.context.getBiome();
            if (current != this.lastBiome) {
                this.lastBiome = current;
                this.lastMatches = current.is(this.biome);
            }
            return this.lastMatches ? this.rule.tryApply(blockX, blockY, blockZ) : null;
        }
    }
}
