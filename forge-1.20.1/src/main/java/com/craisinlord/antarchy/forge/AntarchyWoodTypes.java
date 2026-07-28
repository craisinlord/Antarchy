package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class AntarchyWoodTypes {
    public static final WoodType OURANWOOD = register("ouranwood");
    public static final WoodType PEACH = register("peach");

    private AntarchyWoodTypes() {
    }

    private static WoodType register(String name) {
        return WoodType.register(new WoodType(Antarchy.MODID + ":" + name, BlockSetType.JUNGLE));
    }
}
