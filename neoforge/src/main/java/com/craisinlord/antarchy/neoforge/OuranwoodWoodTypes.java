package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class OuranwoodWoodTypes {
    public static final WoodType OURANWOOD = register("ouranwood");

    private OuranwoodWoodTypes() {
    }

    private static WoodType register(String name) {
        return WoodType.register(new WoodType(Antarchy.MODID + ":" + name, BlockSetType.JUNGLE));
    }
}
