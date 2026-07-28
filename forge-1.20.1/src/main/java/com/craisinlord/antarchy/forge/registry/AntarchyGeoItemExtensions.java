package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.content.client.AntarchyGeoItem;
import com.craisinlord.antarchy.content.client.AntarchyGeoItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class AntarchyGeoItemExtensions {
    private AntarchyGeoItemExtensions() {}

    private static BlockEntityWithoutLevelRenderer geoRendererOf(Item item) {
        return ((AntarchyGeoItemRenderer) ((AntarchyGeoItem) item).getRenderProvider().get()).getCustomRenderer();
    }

    public static IClientItemExtensions plain(Item item) {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return geoRendererOf(item);
            }
        };
    }

    public static IClientItemExtensions crossbowHold(Item item) {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return geoRendererOf(item);
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }
        };
    }

    public static IClientItemExtensions sizeRay(Item item) {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return geoRendererOf(item);
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving.isUsingItem() && entityLiving.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                return null;
            }
        };
    }
}
