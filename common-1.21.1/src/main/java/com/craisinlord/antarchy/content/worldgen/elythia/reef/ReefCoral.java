package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

record ReefCoral(Block block, Block plant, Block floorFan, Block wallFan, Block deadBlock) {

    static final ReefCoral TUBE = new ReefCoral(Blocks.TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.TUBE_CORAL_WALL_FAN, Blocks.DEAD_TUBE_CORAL_BLOCK);
    static final ReefCoral BRAIN = new ReefCoral(Blocks.BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL, Blocks.BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_BLOCK);
    static final ReefCoral BUBBLE = new ReefCoral(Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_BLOCK);
    static final ReefCoral FIRE = new ReefCoral(Blocks.FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL, Blocks.FIRE_CORAL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_BLOCK);
    static final ReefCoral HORN = new ReefCoral(Blocks.HORN_CORAL_BLOCK, Blocks.HORN_CORAL, Blocks.HORN_CORAL_FAN, Blocks.HORN_CORAL_WALL_FAN, Blocks.DEAD_HORN_CORAL_BLOCK);

    private static final ReefCoral[] VANILLA = {TUBE, BRAIN, BUBBLE, FIRE, HORN};

    static ReefCoral star() {
        return new ReefCoral(
                lookup("star_coral_block", Blocks.TUBE_CORAL_BLOCK),
                lookup("star_coral", Blocks.TUBE_CORAL),
                lookup("star_coral_fan", Blocks.TUBE_CORAL_FAN),
                lookup("star_coral_wall_fan", Blocks.TUBE_CORAL_WALL_FAN),
                lookup("dead_star_coral_block", Blocks.DEAD_TUBE_CORAL_BLOCK)
        );
    }

    static ReefCoral pick(RandomSource random) {
        int index = random.nextInt(VANILLA.length + 1);
        return index == VANILLA.length ? star() : VANILLA[index];
    }

    private static Block lookup(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
