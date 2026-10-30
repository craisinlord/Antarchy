package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

public final class ElythiaShared implements DensityFunction {
    public static final MapCodec<ElythiaShared> CODEC = DensityFunction.CODEC.fieldOf("argument")
            .xmap(ElythiaShared::new, ElythiaShared::argument);

    private final DensityFunction argument;
    private final Map<CompileContext, DensitySampler> compiled = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<DfRewriteRule, DensityFunction> rewritten = Collections.synchronizedMap(new WeakHashMap<>());

    public ElythiaShared(DensityFunction argument) {
        this.argument = argument;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        synchronized (this.compiled) {
            return this.compiled.computeIfAbsent(context, this.argument::compileSampler);
        }
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        synchronized (this.rewritten) {
            return this.rewritten.computeIfAbsent(rule, key -> new ElythiaShared(key.rewrite(this.argument)));
        }
    }

    @Override
    public Interval range() {
        return this.argument.range();
    }

    @Override
    public int domainAxes() {
        return this.argument.domainAxes();
    }

    @Override
    public MapCodec<ElythiaShared> codec() {
        return CODEC;
    }
}
