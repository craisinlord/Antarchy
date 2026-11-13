package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.mixins.PoiTypesAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

public final class AntarchyFabricRegistries {
    private AntarchyFabricRegistries() {
    }

    public static void registerAll() {
        AntarchyFabricSounds.SOUND_EVENTS.register();
        AntarchyFabricMisc.ARMOR_MATERIALS.register();
        AntarchyFabricMisc.FLUIDS.register();
        AntarchyFabricBlocks.BLOCKS.register();
        AntarchyFabricBlocks.registerStripping();
        AntarchyFabricEntities.ENTITY_TYPES.register();
        AntarchyFabricMisc.ATTRIBUTES.register();
        AntarchyFabricEntityAttributes.register();
        AntarchyFabricSpawnPlacements.register();
        AntarchyFabricBlocks.BLOCK_ENTITY_TYPES.register();
        AntarchyFabricMisc.PARTICLE_TYPES.register();
        AntarchyFabricMisc.FEATURES.register();
        AntarchyFabricMisc.BIOME_SOURCES.register();
        AntarchyFabricMisc.DENSITY_FUNCTION_TYPES.register();
        AntarchyFabricMisc.MATERIAL_RULES.register();
        AntarchyFabricMisc.STRUCTURE_PLACEMENTS.register();
        AntarchyFabricMisc.ENTITY_SUB_PREDICATES.register();
        AntarchyFabricMisc.MOB_EFFECTS.register();
        AntarchyFabricMisc.POTIONS.register();
        AntarchyFabricItems.ITEMS.register();
        AntarchyFabricMisc.MENUS.register();
        AntarchyFabricMisc.DATA_COMPONENT_TYPES.register();
        AntarchyFabricMisc.RECIPE_SERIALIZERS.register();
        AntarchyFabricCreativeModeTabs.CREATIVE_MODE_TABS.register();
        AntarchyFabricCreativeModeTabs.registerRedstoneTabEntries();
        AntarchyFabricMisc.POI_TYPES.register();
        mapPoiStates();
        AntarchyFabricMisc.VILLAGER_PROFESSIONS.register();
    }

    private static void mapPoiStates() {
        for (DeferredHolder<PoiType, ? extends PoiType> poi : AntarchyFabricMisc.POI_TYPES.getEntries()) {
            Holder<PoiType> holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.wrapAsHolder(poi.get());
            for (BlockState state : poi.get().matchingStates()) {
                PoiTypesAccessor.antarchy$getTypeByState().put(state, holder);
            }
        }
    }
}
