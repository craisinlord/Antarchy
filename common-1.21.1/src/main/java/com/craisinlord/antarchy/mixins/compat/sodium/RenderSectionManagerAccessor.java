package com.craisinlord.antarchy.mixins.compat.sodium;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortBehavior;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.RemovableMultiForest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RenderSectionManager.class, remap = false)
public interface RenderSectionManagerAccessor {
    @Accessor(value = "renderLists", remap = false)
    SortedRenderLists antarchy$getRenderLists();

    @Accessor(value = "renderLists", remap = false)
    void antarchy$setRenderLists(SortedRenderLists renderLists);

    @Accessor(value = "renderableSectionTree", remap = false)
    RemovableMultiForest antarchy$getRenderableSectionTree();

    @Accessor(value = "sectionByPosition", remap = false)
    Long2ReferenceMap<RenderSection> antarchy$getSectionByPosition();

    @Accessor(value = "sortBehavior", remap = false)
    SortBehavior antarchy$getSortBehavior();

    @Invoker(value = "getSearchDistance", remap = false)
    float antarchy$getSearchDistance();
}
