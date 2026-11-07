package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.client.FossilEggColors;
import it.unimi.dsi.fastutil.HashCommon;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

public final class Fossils {
    private static volatile List<Identifier> entities;
    private static volatile List<String> entitiesBlacklist;

    private Fossils() {
    }

    public static List<Identifier> entities() {
        List<String> blacklist = AntarchySettings.fossilEntityBlacklistEntries();
        List<Identifier> cached = entities;
        if (cached == null || entitiesBlacklist != blacklist) {
            List<Identifier> found = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                if (SpawnEggItem.byId(type).isPresent() && !isBlacklisted(key, blacklist)) {
                    found.add(key);
                }
            }
            found.sort(Comparator.comparing(Identifier::toString));
            cached = List.copyOf(found);
            entities = cached;
            entitiesBlacklist = blacklist;
        }
        return cached;
    }

    public static boolean isBlacklisted(Identifier entityId) {
        return isBlacklisted(entityId, AntarchySettings.fossilEntityBlacklistEntries());
    }

    private static boolean isBlacklisted(Identifier entityId, List<String> blacklist) {
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
    public static Identifier resolve(@Nullable Identifier entityId) {
        if (entityId == null || !isBlacklisted(entityId)) {
            return entityId;
        }
        List<Identifier> list = entities();
        if (list.isEmpty()) {
            return null;
        }
        long mixed = HashCommon.mix((long) entityId.hashCode() ^ 0x5DEECE66DL);
        return list.get(Math.floorMod(mixed, list.size()));
    }

    @Nullable
    public static Identifier entityAt(BlockPos pos) {
        List<Identifier> list = entities();
        if (list.isEmpty()) {
            return null;
        }
        long mixed = HashCommon.mix(pos.asLong() ^ 0x5DEECE66DL);
        return list.get(Math.floorMod(mixed, list.size()));
    }

    @Nullable
    public static Identifier entityOf(ItemStack stack) {
        return resolve(stack.get(AntarchyObjects.FOSSIL_ENTITY_COMPONENT.get()));
    }

    @Nullable
    public static SpawnEggItem spawnEgg(@Nullable Identifier entityId) {
        if (entityId == null) {
            return null;
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId)
                .flatMap(type -> SpawnEggItem.byId(type).map(Holder::value))
                .filter(SpawnEggItem.class::isInstance)
                .map(SpawnEggItem.class::cast)
                .orElse(null);
    }

    public static ItemStack spawnEggStack(@Nullable Identifier entityId) {
        SpawnEggItem egg = spawnEgg(entityId);
        return egg == null ? ItemStack.EMPTY : new ItemStack(egg);
    }

    public static int color(@Nullable Identifier entityId, int tintIndex) {
        if (entityId == null || tintIndex < 0 || tintIndex > 1) {
            return -1;
        }
        SpawnEggItem egg = spawnEgg(entityId);
        return egg == null ? fallbackColor(entityId, tintIndex) : FossilEggColors.color(egg, tintIndex).orElseGet(() -> fallbackColor(entityId, tintIndex));
    }

    private static int fallbackColor(Identifier entityId, int tintIndex) {
        double hue = Math.floorMod(entityId.toString().hashCode() + (tintIndex == 0 ? 0 : 72), 360);
        double saturation = tintIndex == 0 ? 0.72D : 0.66D;
        double lightness = tintIndex == 0 ? 0.52D : 0.64D;
        double chroma = (1.0D - Math.abs(2.0D * lightness - 1.0D)) * saturation;
        double section = hue / 60.0D;
        double secondary = chroma * (1.0D - Math.abs(section % 2.0D - 1.0D));
        double red;
        double green;
        double blue;
        if (section < 1.0D) {
            red = chroma;
            green = secondary;
            blue = 0.0D;
        } else if (section < 2.0D) {
            red = secondary;
            green = chroma;
            blue = 0.0D;
        } else if (section < 3.0D) {
            red = 0.0D;
            green = chroma;
            blue = secondary;
        } else if (section < 4.0D) {
            red = 0.0D;
            green = secondary;
            blue = chroma;
        } else if (section < 5.0D) {
            red = secondary;
            green = 0.0D;
            blue = chroma;
        } else {
            red = chroma;
            green = 0.0D;
            blue = secondary;
        }
        double offset = lightness - chroma * 0.5D;
        int r = (int) Math.round((red + offset) * 255.0D);
        int g = (int) Math.round((green + offset) * 255.0D);
        int b = (int) Math.round((blue + offset) * 255.0D);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
