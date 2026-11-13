package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.SnoutCompassAngle;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record SnoutCompassAngleProperty() implements RangeSelectItemModelProperty {
    public static final MapCodec<SnoutCompassAngleProperty> MAP_CODEC = MapCodec.unit(new SnoutCompassAngleProperty());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        return SnoutCompassAngle.angle(itemStack, level, owner instanceof Entity entity ? entity : null, seed);
    }

    @Override
    public MapCodec<SnoutCompassAngleProperty> type() {
        return MAP_CODEC;
    }
}
