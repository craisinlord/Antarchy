package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;
import org.jetbrains.annotations.Nullable;

public final class ElythiaBiomeDispatchRule {
    private static final RuleEvaluator[] NO_RULES = new RuleEvaluator[0];

    private ElythiaBiomeDispatchRule() {
    }

    public record Branch(Optional<List<ResourceKey<Biome>>> biomeIs, boolean invert, MaterialRule thenRun) {
        private static final Codec<Branch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("biome_is").forGetter(Branch::biomeIs),
                Codec.BOOL.optionalFieldOf("invert", false).forGetter(Branch::invert),
                MaterialRule.CODEC.fieldOf("then_run").forGetter(Branch::thenRun)
        ).apply(instance, Branch::new));
    }

    public record Source(List<Branch> branches) implements MaterialRule {
        public static final MapCodec<Source> CODEC = Branch.CODEC.listOf().fieldOf("branches").xmap(Source::new, Source::branches);

        @Override
        public MapCodec<Source> codec() {
            return CODEC;
        }

        @Override
        public RuleEvaluator compile(MaterialRuleContext context) {
            RuleEvaluator[] rules = new RuleEvaluator[this.branches.size()];
            for (int i = 0; i < rules.length; i++) {
                rules[i] = this.branches.get(i).thenRun().compile(context);
            }
            return new Rule(context, this.branches, rules);
        }
    }

    private static final class Rule implements RuleEvaluator {
        private final MaterialRuleContext context;
        private final List<Branch> branches;
        private final RuleEvaluator[] rules;
        private final Reference2ObjectOpenHashMap<Holder<Biome>, RuleEvaluator[]> byBiome = new Reference2ObjectOpenHashMap<>();
        @Nullable
        private Holder<Biome> lastBiome;
        private RuleEvaluator[] lastRules = NO_RULES;
        private int lastX = Integer.MIN_VALUE;
        private int lastZ = Integer.MIN_VALUE;

        private Rule(MaterialRuleContext context, List<Branch> branches, RuleEvaluator[] rules) {
            this.context = context;
            this.branches = branches;
            this.rules = rules;
        }

        @Nullable
        @Override
        public BlockState tryApply(int x, int y, int z) {
            if (x != this.lastX || z != this.lastZ) {
                this.lastX = x;
                this.lastZ = z;
                Holder<Biome> biome = this.context.getBiome();
                if (biome != this.lastBiome) {
                    this.lastBiome = biome;
                    RuleEvaluator[] selected = this.byBiome.get(biome);
                    if (selected == null) {
                        selected = this.select(biome);
                        this.byBiome.put(biome, selected);
                    }
                    this.lastRules = selected;
                }
            }
            for (RuleEvaluator rule : this.lastRules) {
                BlockState state = rule.tryApply(x, y, z);
                if (state != null) {
                    return state;
                }
            }
            return null;
        }

        private RuleEvaluator[] select(Holder<Biome> biome) {
            List<RuleEvaluator> selected = new ArrayList<>();
            for (int i = 0; i < this.rules.length; i++) {
                Branch branch = this.branches.get(i);
                if (branch.biomeIs().isEmpty()) {
                    selected.add(this.rules[i]);
                    continue;
                }
                Set<ResourceKey<Biome>> biomes = Set.copyOf(branch.biomeIs().get());
                if (biome.is(biomes::contains) != branch.invert()) {
                    selected.add(this.rules[i]);
                }
            }
            return selected.toArray(NO_RULES);
        }
    }
}
