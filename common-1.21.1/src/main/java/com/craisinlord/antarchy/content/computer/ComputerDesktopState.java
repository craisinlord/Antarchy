package com.craisinlord.antarchy.content.computer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ComputerDesktopState {
    public static final ResourceLocation DEFAULT_WALLPAPER = ResourceLocation.fromNamespaceAndPath("antarchy", "grid_ant");
    public static final int MAX_UNLOCKED_WALLPAPERS = 64;
    public static final int MAX_INSTALLED_PROGRAMS = 64;
    public static final int MAX_ICON_POSITIONS = 64;
    public static final int MAX_ID_LENGTH = 128;
    public static final int MIN_ICON_COORDINATE = 0;
    public static final int MAX_ICON_COORDINATE = 4096;
    private static final String SELECTED_WALLPAPER_TAG = "SelectedWallpaper";
    private static final String UNLOCKED_WALLPAPERS_TAG = "UnlockedWallpapers";
    private static final String INSTALLED_PROGRAMS_TAG = "InstalledPrograms";
    private static final String ICON_POSITIONS_TAG = "IconPositions";
    private static final String ID_TAG = "Id";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private ResourceLocation selectedWallpaper;
    private final Set<ResourceLocation> unlockedWallpaperIds = new LinkedHashSet<>();
    private final Set<ResourceLocation> installedProgramIds = new LinkedHashSet<>();
    private final Map<ResourceLocation, IconPosition> iconPositions = new LinkedHashMap<>();

    public ResourceLocation selectedWallpaper() {
        return selectedWallpaper;
    }

    public Set<ResourceLocation> unlockedWallpaperIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(unlockedWallpaperIds));
    }

    public Set<ResourceLocation> installedProgramIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(installedProgramIds));
    }

    public Map<ResourceLocation, IconPosition> iconPositions() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(iconPositions));
    }

    public boolean hasWallpaper(ResourceLocation id) {
        return id != null && unlockedWallpaperIds.contains(id);
    }

    public boolean hasProgram(ResourceLocation id) {
        return id != null && installedProgramIds.contains(id);
    }

    public IconPosition iconPosition(ResourceLocation id) {
        return iconPositions.get(id);
    }

    public boolean unlockWallpaper(ResourceLocation id) {
        if (!validId(id) || unlockedWallpaperIds.contains(id)) {
            return false;
        }
        if (unlockedWallpaperIds.size() >= MAX_UNLOCKED_WALLPAPERS) {
            return false;
        }
        return unlockedWallpaperIds.add(id);
    }

    public boolean installProgram(ResourceLocation id) {
        if (!validId(id) || installedProgramIds.size() >= MAX_INSTALLED_PROGRAMS && !installedProgramIds.contains(id)) {
            return false;
        }
        return installedProgramIds.add(id);
    }

    public boolean uninstallProgram(ResourceLocation id) {
        if (id == null || !installedProgramIds.remove(id)) {
            return false;
        }
        iconPositions.remove(id);
        return true;
    }

    public boolean selectWallpaper(ResourceLocation id) {
        if (id != null && !unlockedWallpaperIds.contains(id)) {
            return false;
        }
        if (id == null ? selectedWallpaper == null : id.equals(selectedWallpaper)) {
            return false;
        }
        selectedWallpaper = id;
        return true;
    }

    public boolean setIconPosition(ResourceLocation id, int x, int y) {
        if (!validId(id) || x < MIN_ICON_COORDINATE || x > MAX_ICON_COORDINATE || y < MIN_ICON_COORDINATE || y > MAX_ICON_COORDINATE) {
            return false;
        }
        IconPosition position = new IconPosition(x, y);
        if (position.equals(iconPositions.get(id))) {
            return false;
        }
        if (!iconPositions.containsKey(id) && iconPositions.size() >= MAX_ICON_POSITIONS) {
            return false;
        }
        iconPositions.put(id, position);
        return true;
    }

    public boolean removeIconPosition(ResourceLocation id) {
        return id != null && iconPositions.remove(id) != null;
    }

    public void clear() {
        unlockedWallpaperIds.clear();
        installedProgramIds.clear();
        iconPositions.clear();
        unlockedWallpaperIds.add(DEFAULT_WALLPAPER);
        selectedWallpaper = DEFAULT_WALLPAPER;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (selectedWallpaper != null) {
            tag.putString(SELECTED_WALLPAPER_TAG, selectedWallpaper.toString());
        }
        tag.put(UNLOCKED_WALLPAPERS_TAG, saveIds(unlockedWallpaperIds));
        tag.put(INSTALLED_PROGRAMS_TAG, saveIds(installedProgramIds));
        ListTag positions = new ListTag();
        for (Map.Entry<ResourceLocation, IconPosition> entry : iconPositions.entrySet()) {
            CompoundTag position = new CompoundTag();
            position.putString(ID_TAG, entry.getKey().toString());
            position.putInt(X_TAG, entry.getValue().x());
            position.putInt(Y_TAG, entry.getValue().y());
            positions.add(position);
        }
        tag.put(ICON_POSITIONS_TAG, positions);
        return tag;
    }

    public void load(CompoundTag tag) {
        clear();
        loadIds(tag.getList(UNLOCKED_WALLPAPERS_TAG, 8), unlockedWallpaperIds, MAX_UNLOCKED_WALLPAPERS);
        ResourceLocation storedSelection = parseId(tag.getString(SELECTED_WALLPAPER_TAG));
        selectedWallpaper = storedSelection != null && unlockedWallpaperIds.contains(storedSelection) ? storedSelection : DEFAULT_WALLPAPER;
        loadIds(tag.getList(INSTALLED_PROGRAMS_TAG, 8), installedProgramIds, MAX_INSTALLED_PROGRAMS);
        ListTag positions = tag.getList(ICON_POSITIONS_TAG, 10);
        for (int index = 0; index < positions.size() && iconPositions.size() < MAX_ICON_POSITIONS; index++) {
            CompoundTag stored = positions.getCompound(index);
            ResourceLocation id = parseId(stored.getString(ID_TAG));
            int x = stored.getInt(X_TAG);
            int y = stored.getInt(Y_TAG);
            if (id != null && x >= MIN_ICON_COORDINATE && x <= MAX_ICON_COORDINATE && y >= MIN_ICON_COORDINATE && y <= MAX_ICON_COORDINATE) {
                iconPositions.putIfAbsent(id, new IconPosition(x, y));
            }
        }
    }

    private static ListTag saveIds(Collection<ResourceLocation> ids) {
        ListTag list = new ListTag();
        for (ResourceLocation id : ids) {
            list.add(StringTag.valueOf(id.toString()));
        }
        return list;
    }

    private static void loadIds(ListTag list, Set<ResourceLocation> destination, int limit) {
        for (int index = 0; index < list.size() && destination.size() < limit; index++) {
            ResourceLocation id = parseId(list.getString(index));
            if (id != null) {
                destination.add(id);
            }
        }
    }

    private static ResourceLocation parseId(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_ID_LENGTH) {
            return null;
        }
        try {
            return ResourceLocation.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean validId(ResourceLocation id) {
        return id != null && id.toString().length() <= MAX_ID_LENGTH;
    }

    public record IconPosition(int x, int y) {
    }
}
