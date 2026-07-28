package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.creative.CreativeTabContents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegistryObject;

public class AntarchyForgeCreativeModeTabs {
    private AntarchyForgeCreativeModeTabs() {}

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Antarchy.MODID);

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(AntarchyForgeCreativeModeTabs::buildCreativeTabs);
    }

    public static final RegistryObject<CreativeModeTab> ANTARCHY_TAB = CREATIVE_MODE_TABS.register("antarchy",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.antarchy.antarchy"))
                    .icon(() -> new ItemStack(AntarchyForgeItems.BIG_BERTHA.get()))
                    .displayItems((parameters, output) -> {})
                    .build());

    static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ANTARCHY_TAB.getKey()) {
            CreativeTabContents.populateAntarchyTab(new CreativeTabContents.AntarchyTabOutput() {
                @Override
                public void accept(net.minecraft.world.level.ItemLike item) {
                    event.accept(item);
                }

                @Override
                public void accept(ItemStack stack) {
                    event.accept(stack);
                }
            });
            return;
        }
        CreativeTabContents.populateNeoForgeVanillaTab(event.getTabKey(), event.getParameters().holders(),
                new CreativeTabContents.NeoForgeVanillaTabOutput() {
                    @Override
                    public void accept(net.minecraft.world.level.ItemLike item) {
                        event.accept(item);
                    }

                    @Override
                    public void accept(ItemStack stack) {
                        event.accept(stack);
                    }

                    @Override
                    public void insertAfter(net.minecraft.world.level.ItemLike anchor, net.minecraft.world.level.ItemLike item) {
                        event.getEntries().putAfter(new ItemStack(anchor), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    }
                });
    }
}
