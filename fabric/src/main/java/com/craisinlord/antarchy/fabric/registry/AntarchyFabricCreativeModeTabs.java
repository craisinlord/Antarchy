package com.craisinlord.antarchy.fabric.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.creative.CreativeTabContents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class AntarchyFabricCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Antarchy.MODID);



    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANTARCHY_TAB = CREATIVE_MODE_TABS.register("antarchy",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.antarchy.antarchy"))
                    .icon(() -> new ItemStack(AntarchyFabricItems.GRAVITY_GUN.get()))
                    .displayItems((parameters, output) -> populateCreativeTab(output))
                    .build());



    private static void populateCreativeTab(CreativeModeTab.Output output) {
        CreativeTabContents.populateAntarchyTab(new CreativeTabContents.AntarchyTabOutput() {
            @Override
            public void accept(net.minecraft.world.level.ItemLike item) {
                output.accept(item);
            }

            @Override
            public void accept(ItemStack stack) {
                output.accept(stack);
            }
        });
    }


    public static void register() {
        CREATIVE_MODE_TABS.register();
    }

}
