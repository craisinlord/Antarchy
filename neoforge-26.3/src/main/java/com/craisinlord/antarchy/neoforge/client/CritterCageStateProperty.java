package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.content.item.CritterCageItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CritterCageStateProperty implements RangeSelectItemModelProperty {
    public static final CritterCageStateProperty INSTANCE = new CritterCageStateProperty();
    public static final MapCodec<CritterCageStateProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

    private CritterCageStateProperty() {
    }

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
