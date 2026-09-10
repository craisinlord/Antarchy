package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import net.minecraft.world.entity.EntityType;

public class PrinceEggBlock extends RoyalEggBlock {

    public PrinceEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public EntityType<? extends RoyalMountEntity> mountType() {
        return AntarchyObjects.PRINCE.get();
    }
}
