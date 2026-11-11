package com.craisinlord.antarchy.content.block;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;

public final class RockiteTint {
    private static final int[] HEIGHTS = {-64, -20, 20, 60, 100, 160, 230, 304};
    private static final int[] COLORS = {0x312351, 0x413273, 0x4C4485, 0x545C8A, 0x5B6173, 0x7C8AB0, 0xA9BBE4, 0xDBEBFF};
    public static final int DEFAULT_COLOR = 0xFF5B6173;

    private static final List<String> TINTED = List.of(
            "rockite", "rockite_slab", "rockite_stairs", "rockite_wall",
            "polished_rockite", "polished_rockite_slab", "polished_rockite_stairs", "polished_rockite_wall",
            "rockite_bricks", "cracked_rockite_bricks", "chiseled_rockite_bricks",
            "rockite_brick_slab", "rockite_brick_stairs", "rockite_brick_wall",
            "rockite_pillar"
    );

    private static final List<String> OVERLAID = List.of(
            "mossy_rockite_bricks", "mossy_rockite_brick_slab", "mossy_rockite_brick_stairs", "mossy_rockite_brick_wall",
            "rockite_coal_ore", "rockite_copper_ore", "rockite_iron_ore", "rockite_gold_ore", "rockite_redstone_ore",
            "rockite_lapis_ore", "rockite_diamond_ore", "rockite_titanium_ore", "rockite_uranium_ore"
    );

    private RockiteTint() {
    }

    public static int color(int y) {
        if (y <= HEIGHTS[0]) {
            return 0xFF000000 | COLORS[0];
        }
        for (int i = 1; i < HEIGHTS.length; i++) {
            if (y < HEIGHTS[i]) {
                float t = (y - HEIGHTS[i - 1]) / (float) (HEIGHTS[i] - HEIGHTS[i - 1]);
                return 0xFF000000 | blend(COLORS[i - 1], COLORS[i], t);
            }
        }
        return 0xFF000000 | COLORS[COLORS.length - 1];
    }

    public static int color(BlockPos pos, int tintIndex) {
        if (tintIndex != 0) {
            return -1;
        }
        return pos == null ? DEFAULT_COLOR : color(pos.getY());
    }

    public static int itemColor(int tintIndex) {
        return tintIndex == 0 ? DEFAULT_COLOR : -1;
    }

    public static Block[] blocks() {
        return resolve(TINTED, OVERLAID);
    }

    public static Block[] overlaidBlocks() {
        return resolve(OVERLAID);
    }

    @SafeVarargs
    private static Block[] resolve(List<String>... groups) {
        return Arrays.stream(groups)
                .flatMap(List::stream)
                .map(ElythiaPalette::block)
                .toArray(Block[]::new);
    }

    private static int blend(int from, int to, float t) {
        int r = Math.round(Mth.lerp(t, from >> 16 & 0xFF, to >> 16 & 0xFF));
        int g = Math.round(Mth.lerp(t, from >> 8 & 0xFF, to >> 8 & 0xFF));
        int b = Math.round(Mth.lerp(t, from & 0xFF, to & 0xFF));
        return r << 16 | g << 8 | b;
    }
}
