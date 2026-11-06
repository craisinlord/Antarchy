package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.block.Fossils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record FossilTintSource(int layer) implements ItemTintSource {
    public static final MapCodec<FossilTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.INT.fieldOf("layer").forGetter(FossilTintSource::layer))
                    .apply(instance, FossilTintSource::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return Fossils.color(Fossils.entityOf(itemStack), this.layer);
    }

    @Override
    public MapCodec<FossilTintSource> type() {
        return MAP_CODEC;
    }
}
