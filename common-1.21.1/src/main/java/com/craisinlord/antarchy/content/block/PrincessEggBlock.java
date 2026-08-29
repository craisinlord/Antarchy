package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EntityType;

public class PrincessEggBlock extends RoyalEggBlock {
    public static final MapCodec<PrincessEggBlock> CODEC = simpleCodec(PrincessEggBlock::new);

    public PrincessEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PrincessEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected EntityType<? extends RoyalMountEntity> mountType() {
        return AntarchyObjects.PRINCESS.get();
    }
}
