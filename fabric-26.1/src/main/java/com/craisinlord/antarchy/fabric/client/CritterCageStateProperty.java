package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.CritterCageItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record CritterCageStateProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<CritterCageStateProperty> MAP_CODEC = MapCodec.unit(new CritterCageStateProperty());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (!(itemStack.getItem() instanceof CritterCageItem cage)) {
            return 0.0F;
        }
        return cage.getItemState(itemStack);
    }

    @Override
    public MapCodec<CritterCageStateProperty> type() {
        return MAP_CODEC;
    }
}
