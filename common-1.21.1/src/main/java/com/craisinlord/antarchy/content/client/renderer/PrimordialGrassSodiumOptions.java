package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@ConfigEntryPointForge(Antarchy.MODID)
public final class PrimordialGrassSodiumOptions implements ConfigEntryPoint {
    public enum Quality {
        LOW,
        MEDIUM,
        HIGH
    }

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions().addPage(builder.createOptionPage()
                .setName(Component.translatable("options.antarchy.grass_page"))
                .addOptionGroup(builder.createOptionGroup()
                        .addOption(builder.createBooleanOption(id("dynamic_grass"))
                                .setName(Component.translatable("options.antarchy.dynamic_grass"))
                                .setTooltip(Component.translatable("options.antarchy.dynamic_grass.tooltip"))
                                .setStorageHandler(AntarchySettings::savePrimordialGrass)
                                .setDefaultValue(true)
                                .setBinding(AntarchySettings::setPrimordialGrassDynamic, AntarchySettings::primordialGrassDynamic))
                        .addOption(builder.createEnumOption(id("grass_quality"), Quality.class)
                                .setName(Component.translatable("options.antarchy.grass_quality"))
                                .setTooltip(Component.translatable("options.antarchy.grass_quality.tooltip"))
                                .setStorageHandler(AntarchySettings::savePrimordialGrass)
                                .setDefaultValue(Quality.MEDIUM)
                                .setElementNameProvider(PrimordialGrassSodiumOptions::qualityName)
                                .setBinding(
                                        value -> AntarchySettings.setPrimordialGrassQuality(value.ordinal()),
                                        () -> Quality.values()[AntarchySettings.primordialGrassQuality()]))));
    }

    private static Component qualityName(Quality quality) {
        return Component.translatable("options.antarchy.grass_quality." + quality.name().toLowerCase(java.util.Locale.ROOT));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
    }
}
