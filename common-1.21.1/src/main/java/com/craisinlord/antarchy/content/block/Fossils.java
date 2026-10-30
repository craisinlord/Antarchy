package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.FossilBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class Fossils {
    private static volatile List<ResourceLocation> entities;

    private Fossils() {
    }

    public static List<ResourceLocation> entities() {
        List<ResourceLocation> cached = entities;
        if (cached == null) {
            List<ResourceLocation> found = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (SpawnEggItem.byId(type) != null) {
                    found.add(BuiltInRegistries.ENTITY_TYPE.getKey(type));
                }
            }
            found.sort(Comparator.comparing(ResourceLocation::toString));
            cached = List.copyOf(found);
            entities = cached;
        }
        return cached;
    }

    @Nullable
    public static ResourceLocation entityAt(BlockPos pos) {
        List<ResourceLocation> list = entities();
        if (list.isEmpty()) {
            return null;
        }
        long mixed = HashCommon.mix(pos.asLong() ^ 0x5DEECE66DL);
        return list.get(Math.floorMod(mixed, list.size()));
    }

    @Nullable
    public static ResourceLocation entityAt(BlockAndTintGetter level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FossilBlockEntity fossil) {
            return fossil.entityId();
        }
        return entityAt(pos);
    }

    @Nullable
    public static ResourceLocation entityOf(ItemStack stack) {
        return stack.get(AntarchyObjects.FOSSIL_ENTITY_COMPONENT.get());
    }

    @Nullable
    public static SpawnEggItem spawnEgg(@Nullable ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).map(SpawnEggItem::byId).orElse(null);
    }

    public static ItemStack spawnEggStack(@Nullable ResourceLocation entityId) {
        SpawnEggItem egg = spawnEgg(entityId);
        return egg == null ? ItemStack.EMPTY : new ItemStack(egg);
    }

    public static int color(@Nullable ResourceLocation entityId, int tintIndex) {
        SpawnEggItem egg = spawnEgg(entityId);
        if (egg == null || tintIndex < 0 || tintIndex > 1) {
            return -1;
        }
        return FastColor.ARGB32.opaque(egg.getColor(tintIndex));
    }
}
