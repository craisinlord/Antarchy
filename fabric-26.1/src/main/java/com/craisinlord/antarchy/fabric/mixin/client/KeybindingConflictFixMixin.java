package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.fabric.client.KeybindingConflictFixStore;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public abstract class KeybindingConflictFixMixin {
    @Shadow @Final private static Map<InputConstants.Key, List<KeyMapping>> MAP;
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow protected InputConstants.Key key;
    @Shadow private boolean isDown;
    @Shadow private int clickCount;

    @Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;I)V", at = @At("TAIL"))
    private void antarchy$addInitialMapping(String name, InputConstants.Type type, int key, KeyMapping.Category category, int order, CallbackInfo ci) {
        KeybindingConflictFixStore.add(this.key, (KeyMapping) (Object) this);
    }

    @Inject(method = "resetMapping", at = @At("TAIL"))
    private static void antarchy$rebuildMappings(CallbackInfo ci) {
        KeybindingConflictFixStore.clear();
        for (KeyMapping mapping : ALL.values()) {
            KeybindingConflictFixStore.add(((KeybindingConflictFixMixin) (Object) mapping).key, mapping);
        }
    }

    @Inject(method = "set", at = @At("TAIL"))
    private static void antarchy$setConflicts(InputConstants.Key key, boolean down, CallbackInfo ci) {
        if (!AntarchySettings.fabricKeybindingConflictFixEnabled()) {
            return;
        }
        List<KeyMapping> handled = antarchy$vanillaMappings(key);
        KeyMapping active = antarchy$active(handled);
        for (KeyMapping mapping : KeybindingConflictFixStore.others(key, active)) {
            if (!handled.contains(mapping)) {
                ((KeybindingConflictFixMixin) (Object) mapping).antarchy$setDown(down);
            }
        }
    }

    @Inject(method = "click", at = @At("TAIL"))
    private static void antarchy$clickConflicts(InputConstants.Key key, CallbackInfo ci) {
        if (!AntarchySettings.fabricKeybindingConflictFixEnabled()) {
            return;
        }
        List<KeyMapping> handled = antarchy$vanillaMappings(key);
        KeyMapping active = antarchy$active(handled);
        for (KeyMapping mapping : KeybindingConflictFixStore.others(key, active)) {
            if (!handled.contains(mapping) && ((KeybindingConflictFixMixin) (Object) mapping).antarchy$isSprint()) {
                ((KeybindingConflictFixMixin) (Object) mapping).antarchy$click();
            }
        }
    }

    @Unique
    private static List<KeyMapping> antarchy$vanillaMappings(InputConstants.Key key) {
        List<KeyMapping> mappings = MAP.get(key);
        return mappings == null ? List.of() : mappings;
    }

    @Unique
    private static KeyMapping antarchy$active(List<KeyMapping> mappings) {
        return mappings.isEmpty() ? null : mappings.get(mappings.size() - 1);
    }

    @Unique
    private void antarchy$setDown(boolean down) {
        this.isDown = down;
    }

    @Unique
    private boolean antarchy$isSprint() {
        return KeybindingConflictFixStore.isSprint((KeyMapping) (Object) this);
    }

    @Unique
    private void antarchy$click() {
        this.clickCount++;
    }
}
