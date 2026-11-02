package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.block.entity.FossilBlockEntity;
import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class Fossils {
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String ENTITY_TAG = "Entity";

    private static volatile Map<ResourceLocation, SpawnEggItem> eggs;
    private static volatile List<ResourceLocation> entities;
    private static volatile List<String> entitiesBlacklist;

    private Fossils() {
    }

    private static Map<ResourceLocation, SpawnEggItem> eggs() {
        Map<ResourceLocation, SpawnEggItem> cached = eggs;
        if (cached == null) {
            Map<ResourceLocation, SpawnEggItem> found = new HashMap<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof SpawnEggItem egg) {
                    EntityType<?> type = egg.getType(null);
                    if (type != null) {
                        found.putIfAbsent(BuiltInRegistries.ENTITY_TYPE.getKey(type), egg);
                    }
                }
            }
            cached = Map.copyOf(found);
            eggs = cached;
        }
        return cached;
    }

    public static List<ResourceLocation> entities() {
        List<String> blacklist = AntarchySettings.fossilEntityBlacklistEntries();
        List<ResourceLocation> cached = entities;
        if (cached == null || entitiesBlacklist != blacklist) {
            List<ResourceLocation> found = new ArrayList<>();
            Map<ResourceLocation, SpawnEggItem> known = eggs();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                if (known.containsKey(key) && !isBlacklisted(key, blacklist)) {
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
        CompoundTag tag = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (tag == null || !tag.contains(ENTITY_TAG, Tag.TAG_STRING)) {
            return null;
        }
        return resolve(ResourceLocation.tryParse(tag.getString(ENTITY_TAG)));
    }

    public static void setEntity(ItemStack stack, ResourceLocation entityId) {
        stack.getOrCreateTagElement(BLOCK_ENTITY_TAG).putString(ENTITY_TAG, entityId.toString());
    }

    @Nullable
    public static SpawnEggItem spawnEgg(@Nullable ResourceLocation entityId) {
        if (entityId == null) {
            return null;
        }
        return eggs().get(entityId);
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
        return 0xFF000000 | egg.getColor(tintIndex);
    }
}
