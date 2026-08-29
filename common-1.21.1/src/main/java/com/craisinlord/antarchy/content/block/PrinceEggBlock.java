package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EntityType;

public class PrinceEggBlock extends RoyalEggBlock {
    public static final MapCodec<PrinceEggBlock> CODEC = simpleCodec(PrinceEggBlock::new);

    public PrinceEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PrinceEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected EntityType<? extends RoyalMountEntity> mountType() {
        return AntarchyObjects.PRINCE.get();
    }
}
