package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Set;
import java.util.function.Consumer;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class InfinityInterfaceHooks {
    private InfinityInterfaceHooks() {
    }

    public static boolean isRemovedOrBlockedBook(ItemEntity itemEntity) {
        if (itemEntity.isRemoved()) {
            return true;
        }
        if (!AntarchySettings.disableInfinityBookPortalCreation()) {
            return false;
        }
        ItemStack stack = itemEntity.getItem();
        return stack.is(Items.WRITABLE_BOOK) || stack.is(Items.WRITTEN_BOOK);
    }

    public static boolean tryAddInfinityDimension(MinecraftServer server, ResourceLocation dimensionId) {
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        if (server instanceof MinecraftServerAccess access && access.infinity$hasToAdd(key)) {
            return true;
        }
        try {
            return PortalCreator.tryAddInfinityDimension(server, dimensionId);
        } catch (RuntimeException | Error throwable) {
            InfinityGenerationFailure.mark(dimensionId);
            throw throwable;
        }
    }

    @SuppressWarnings("unchecked")
    public static void scanBlockEntities(Set<?> entries, Consumer<?> scan) {
        Set<ResourceKey<BlockEntityType<?>>> keys = (Set<ResourceKey<BlockEntityType<?>>>) entries;
        Consumer<ResourceKey<BlockEntityType<?>>> action = (Consumer<ResourceKey<BlockEntityType<?>>>) scan;
        InfinityBlockEntityScanGuard.forEach(keys, action, (entry, error) ->
                Antarchy.LOGGER.warn(
                        "[Antarchy] Skipping block entity {} in Infinite Dimensions' config scan because its factory has a linkage error. Check the owning mod's dependency versions; normal block placement is still affected.",
                        entry.location(), error));
    }
}
