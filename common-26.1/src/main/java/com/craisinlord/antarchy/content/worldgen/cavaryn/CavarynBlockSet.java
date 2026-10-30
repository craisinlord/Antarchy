package com.craisinlord.antarchy.content.worldgen.cavaryn;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CavarynBlockSet {
    private final Set<Identifier> ids;
    private volatile Set<Block> blocks;

    public CavarynBlockSet(Set<Identifier> ids) {
        this.ids = ids;
    }

    public boolean contains(BlockState state) {
        Set<Block> resolved = this.blocks;
        if (resolved == null) {
            resolved = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Identifier id : this.ids) {
                BuiltInRegistries.BLOCK.getOptional(id).ifPresent(resolved::add);
            }
            this.blocks = resolved;
        }
        return resolved.contains(state.getBlock());
    }
}
