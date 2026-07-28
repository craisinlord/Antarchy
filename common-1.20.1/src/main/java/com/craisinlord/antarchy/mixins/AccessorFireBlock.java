package com.craisinlord.antarchy.mixins;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireBlock.class)
public interface AccessorFireBlock {
    @Accessor("igniteOdds")
    Object2IntMap<Block> antarchy$getIgniteOdds();

    @Accessor("burnOdds")
    Object2IntMap<Block> antarchy$getBurnOdds();
}
