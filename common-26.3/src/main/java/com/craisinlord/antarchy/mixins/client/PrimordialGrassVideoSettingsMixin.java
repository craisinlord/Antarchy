package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class PrimordialGrassVideoSettingsMixin extends OptionsSubScreen {
    private static final String[] ANTARCHY$QUALITY_KEYS = {"low", "medium", "high"};

    protected PrimordialGrassVideoSettingsMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void antarchy$addGrassOptions(CallbackInfo callback) {
        OptionInstance<Boolean> dynamic = OptionInstance.createBoolean(
                "options.antarchy.dynamic_grass",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.antarchy.dynamic_grass.tooltip")),
                AntarchySettings.primordialGrassDynamic(),
                value -> {
                    AntarchySettings.setPrimordialGrassDynamic(value);
                    AntarchySettings.savePrimordialGrass();
                });
        OptionInstance<Integer> quality = new OptionInstance<>(
                "options.antarchy.grass_quality",
                OptionInstance.cachedConstantTooltip(Component.translatable("options.antarchy.grass_quality.tooltip")),
                (caption, value) -> Component.translatable("options.antarchy.grass_quality." + ANTARCHY$QUALITY_KEYS[value]),
                new OptionInstance.Enum<>(List.of(0, 1, 2), Codec.INT),
                AntarchySettings.primordialGrassQuality(),
                value -> {
                    AntarchySettings.setPrimordialGrassQuality(value);
                    AntarchySettings.savePrimordialGrass();
                });
        this.list.addSmall(dynamic, quality);
    }
}
