package com.craisinlord.antarchy.content.client.compat;

import com.craisinlord.antarchy.content.AntarchyObjects;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IrisBlockIdCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/IrisCompat");
    private static boolean warned;

    private IrisBlockIdCompat() {
    }

    @SuppressWarnings("unchecked")
    public static void aliasPrimordialGrass(Object result) {
        try {
            if (!(result instanceof Object2IntMap<?> raw)) {
                return;
            }
            Object2IntMap<BlockState> map = (Object2IntMap<BlockState>) raw;
            BlockState reference = Blocks.SHORT_GRASS.defaultBlockState();
            if (!map.containsKey(reference)) {
                return;
            }
            int id = map.getInt(reference);
            Block grass = AntarchyObjects.PRIMORDIAL_GRASS.get();
            for (BlockState state : grass.getStateDefinition().getPossibleStates()) {
                if (!map.containsKey(state)) {
                    map.put(state, id);
                }
            }
        } catch (Throwable throwable) {
            if (!warned) {
                warned = true;
                LOGGER.warn("Could not give Primordial Grass a shader block ID; it may look dark with shaders", throwable);
            }
        }
    }
}
