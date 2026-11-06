package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.content.item.CritterCageItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CritterCageTintSource(int layer) implements ItemTintSource {
    public static final MapCodec<CritterCageTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.INT.fieldOf("layer").forGetter(CritterCageTintSource::layer))
                    .apply(instance, CritterCageTintSource::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (!(itemStack.getItem() instanceof CritterCageItem cage)) {
            return -1;
        }
        if (cage.getItemState(itemStack) != 2) {
            return -1;
        }
        if (this.layer == 1) {
            return cage.getPrimaryColor(itemStack);
        }
        if (this.layer == 2) {
            return cage.getSecondaryColor(itemStack);
        }
        return -1;
    }

    @Override
    public MapCodec<CritterCageTintSource> type() {
        return MAP_CODEC;
    }
}
