package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Map;

public final class SeparateLargeItemModels {
    private static final Map<Item, ResourceLocation> GUI_MODELS = Map.ofEntries(
            entry(AntarchyFabricItems.ATTITUDE_ADJUSTER.get(), "attitude_adjuster_gui"),
            entry(AntarchyFabricItems.BATTLE_AXE.get(), "battle_axe_gui"),
            entry(AntarchyFabricItems.BIG_BERTHA.get(), "big_bertha_gui"),
            entry(AntarchyFabricItems.EYE_OF_THE_STORM.get(), "eye_of_the_storm_gui"),
            entry(AntarchyFabricItems.GIANT_FRYING_PAN.get(), "giant_frying_pan_gui"),
            entry(AntarchyFabricItems.GRAVITY_GUN.get(), "gravity_gun_gui"),
            entry(AntarchyFabricItems.GROWTH_RAY.get(), "grow_ray_gui"),
            entry(AntarchyFabricItems.KRAKENS_GRASP.get(), "krakens_grasp_gui"),
            entry(AntarchyFabricItems.PORTAL_GUN.get(), "portal_gun_gui"),
            entry(AntarchyFabricItems.ROYAL_ASSAILANT_BATTLEAXE.get(), "royal_assailant_battleaxe_gui"),
            entry(AntarchyFabricItems.ROYAL_ASSAILANT_STAFF.get(), "royal_assailant_staff_gui"),
            entry(AntarchyFabricItems.ROYAL_GUARDIAN_SWORD.get(), "royal_guardian_sword_gui"),
            entry(AntarchyFabricItems.RPO_LAUNCHER.get(), "rpo_launcher_gui"),
            entry(AntarchyFabricItems.SHRINK_RAY.get(), "shrink_ray_gui"),
            entry(AntarchyFabricItems.SQUIDZOOKA.get(), "squidzooka_gui"),
            entry(AntarchyFabricItems.WATER_CANNON.get(), "water_cannon_gui")
    );

    private SeparateLargeItemModels() {
    }

    public static ResourceLocation modelFor(ItemStack stack) {
        return GUI_MODELS.get(stack.getItem());
    }

    public static Collection<ResourceLocation> all() {
        return GUI_MODELS.values();
    }

    private static Map.Entry<Item, ResourceLocation> entry(Item item, String modelPath) {
        return Map.entry(item, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "item/" + modelPath));
    }
}
