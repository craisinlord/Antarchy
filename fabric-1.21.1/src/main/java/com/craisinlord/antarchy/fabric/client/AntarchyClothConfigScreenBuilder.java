package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.fabric.AntarchyConfigModuleFabric;
import com.craisinlord.antarchy.fabric.AntarchyConfigModuleFabric.ConfigSection;
import com.craisinlord.antarchy.fabric.AntarchyConfigModuleFabric.SettingBinding;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Builds a Cloth Config screen directly from {@link AntarchyConfigModuleFabric}'s existing
 * reflection-based binding map, so the screen and the on-disk JSON files stay in sync
 * automatically — no separate hand-authored list of 350+ entries.
 *
 * <p>Only ever reached from {@link AntarchyModMenuIntegration#getModConfigScreenFactory()} after
 * it has already confirmed Cloth Config is present, so every Cloth Config class referenced here
 * is safe to touch.
 *
 * <p>Phase 1 scope (see the implementation plan): raw field names as labels, no min/max bounds,
 * no tooltips, and fields backed by {@code ResourceKey<Level>} (the four dimension-identity
 * settings) are skipped entirely rather than exposed with a widget that doesn't fit them.
 */
public final class AntarchyClothConfigScreenBuilder {
    private AntarchyClothConfigScreenBuilder() {
    }

    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("antarchy.config.title"));
        // Entry save consumers (below) already update the live AntarchySettings fields the
        // instant the player edits a widget, matching how the game normally behaves. This runs
        // last, once, when the player clicks "Save" — it's what actually writes those changes
        // back to the three JSON files, since AntarchyConfigModuleFabric only reads+writes them
        // at startup otherwise.
        builder.setSavingRunnable(AntarchyConfigModuleFabric::persist);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        Map<ConfigSection, ConfigCategory> categories = new EnumMap<>(ConfigSection.class);
        for (ConfigSection section : ConfigSection.values()) {
            categories.put(section, builder.getOrCreateCategory(Component.literal(displayName(section))));
        }

        for (SettingBinding binding : AntarchyConfigModuleFabric.getBindings().values()) {
            addEntry(entryBuilder, categories.get(binding.getSection()), binding);
        }

        return builder.build();
    }

    private static void addEntry(ConfigEntryBuilder entryBuilder, ConfigCategory category, SettingBinding binding) {
        Class<?> type = binding.getType();
        Component label = Component.literal(binding.getName());

        if (type == boolean.class) {
            category.addEntry(entryBuilder.startBooleanToggle(label, (Boolean) binding.getValue())
                    .setSaveConsumer(binding::setValue)
                    .build());
        } else if (type == int.class) {
            category.addEntry(entryBuilder.startIntField(label, (Integer) binding.getValue())
                    .setSaveConsumer(binding::setValue)
                    .build());
        } else if (type == double.class) {
            category.addEntry(entryBuilder.startDoubleField(label, (Double) binding.getValue())
                    .setSaveConsumer(binding::setValue)
                    .build());
        } else if (type == float.class) {
            category.addEntry(entryBuilder.startFloatField(label, (Float) binding.getValue())
                    .setSaveConsumer(binding::setValue)
                    .build());
        }
        // String-typed bindings back the ResourceKey<Level> dimension fields (see
        // AntarchyConfigModuleFabric.isSupported) and are intentionally skipped here — they're
        // structural dimension identity, not a tuning knob, and Cloth Config has no natural
        // widget for them. Every other supported type is exactly what's already persisted to
        // JSON today, so nothing shown here can drift from what the JSON files contain.
    }

    private static String displayName(ConfigSection section) {
        return switch (section) {
            case MOBS -> "Mobs";
            case TOOLS -> "Tools";
            case MISC -> "Misc";
        };
    }
}
