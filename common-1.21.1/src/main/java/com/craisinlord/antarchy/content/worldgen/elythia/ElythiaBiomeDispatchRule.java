package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.mixins.SurfaceRulesContextAccessor;
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
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jetbrains.annotations.Nullable;

public final class ElythiaBiomeDispatchRule extends SurfaceRules {
    private static final SurfaceRule[] NO_RULES = new SurfaceRule[0];

    private ElythiaBiomeDispatchRule() {
    }

    public record Branch(Optional<List<ResourceKey<Biome>>> biomeIs, boolean invert, RuleSource thenRun) {
        private static final Codec<Branch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("biome_is").forGetter(Branch::biomeIs),
                Codec.BOOL.optionalFieldOf("invert", false).forGetter(Branch::invert),
                RuleSource.CODEC.fieldOf("then_run").forGetter(Branch::thenRun)
        ).apply(instance, Branch::new));
    }

    public record Source(List<Branch> branches) implements RuleSource {
        public static final MapCodec<Source> CODEC = Branch.CODEC.listOf().fieldOf("branches").xmap(Source::new, Source::branches);
        private static final KeyDispatchDataCodec<Source> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

        @Override
        public KeyDispatchDataCodec<? extends RuleSource> codec() {
            return KEY_CODEC;
        }

        @Override
        public SurfaceRule apply(Context context) {
            SurfaceRule[] rules = new SurfaceRule[this.branches.size()];
            for (int i = 0; i < rules.length; i++) {
                rules[i] = this.branches.get(i).thenRun().apply(context);
            }
            return new Rule((SurfaceRulesContextAccessor) (Object) context, this.branches, rules);
        }
    }

    private static final class Rule implements SurfaceRule {
        private final SurfaceRulesContextAccessor context;
        private final List<Branch> branches;
        private final SurfaceRule[] rules;
        private final Reference2ObjectOpenHashMap<Holder<Biome>, SurfaceRule[]> byBiome = new Reference2ObjectOpenHashMap<>();
        @Nullable
        private Holder<Biome> lastBiome;
        private SurfaceRule[] lastRules = NO_RULES;
        private int lastX = Integer.MIN_VALUE;
        private int lastZ = Integer.MIN_VALUE;

        private Rule(SurfaceRulesContextAccessor context, List<Branch> branches, SurfaceRule[] rules) {
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
                Holder<Biome> biome = this.context.antarchy$biome().get();
                if (biome != this.lastBiome) {
                    this.lastBiome = biome;
                    SurfaceRule[] selected = this.byBiome.get(biome);
                    if (selected == null) {
                        selected = this.select(biome);
                        this.byBiome.put(biome, selected);
                    }
                    this.lastRules = selected;
                }
            }
            for (SurfaceRule rule : this.lastRules) {
                BlockState state = rule.tryApply(x, y, z);
                if (state != null) {
                    return state;
                }
            }
            return null;
        }

        private SurfaceRule[] select(Holder<Biome> biome) {
            List<SurfaceRule> selected = new ArrayList<>();
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
