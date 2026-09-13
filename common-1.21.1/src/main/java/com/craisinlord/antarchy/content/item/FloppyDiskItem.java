package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class FloppyDiskItem extends Item {
    public FloppyDiskItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation id = stack.get(AntarchyObjects.FLOPPY_DISK_COMPONENT.get());
        if (id != null) {
            var disk = com.craisinlord.antarchy.content.guide.ComputerGuideData.disk(id);
            if (disk != null) {
                return Component.translatable(disk.titleKey());
            }
        }
        return Component.translatable("item.antarchy.floppy_disk");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation id = stack.get(AntarchyObjects.FLOPPY_DISK_COMPONENT.get());
        if (id != null) {
            var disk = com.craisinlord.antarchy.content.guide.ComputerGuideData.disk(id);
            if (disk != null) {
                tooltip.add(Component.translatable("guide.antarchy.category." + disk.category()));
            }
        }
    }
}
