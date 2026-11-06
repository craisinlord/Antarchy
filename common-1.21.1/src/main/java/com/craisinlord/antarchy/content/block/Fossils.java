package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
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
    private static volatile List<String> entitiesBlacklist;

    private Fossils() {
    }

    public static List<ResourceLocation> entities() {
        List<String> blacklist = AntarchySettings.fossilEntityBlacklistEntries();
        List<ResourceLocation> cached = entities;
        if (cached == null || entitiesBlacklist != blacklist) {
            List<ResourceLocation> found = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                if (SpawnEggItem.byId(type) != null && !isBlacklisted(key, blacklist)) {
                    found.add(key);
                }
            }
            found.sort(Comparator.comparing(ResourceLocation::toString));
            cached = List.copyOf(found);
            entities = cached;
            entitiesBlacklist = blacklist;
        }
        return cached;
    }

    public static boolean isBlacklisted(ResourceLocation entityId) {
        return isBlacklisted(entityId, AntarchySettings.fossilEntityBlacklistEntries());
    }

    private static boolean isBlacklisted(ResourceLocation entityId, List<String> blacklist) {
        String id = entityId.toString();
        String namespaceWildcard = entityId.getNamespace() + ":*";
        for (String entry : blacklist) {
            if (entry.equals(id) || entry.equals(namespaceWildcard)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ResourceLocation resolve(@Nullable ResourceLocation entityId) {
        if (entityId == null || !isBlacklisted(entityId)) {
            return entityId;
        }
        List<ResourceLocation> list = entities();
        if (list.isEmpty()) {
            return null;
        }
        long mixed = HashCommon.mix((long) entityId.hashCode() ^ 0x5DEECE66DL);
        return list.get(Math.floorMod(mixed, list.size()));
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
        return resolve(stack.get(AntarchyObjects.FOSSIL_ENTITY_COMPONENT.get()));
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
