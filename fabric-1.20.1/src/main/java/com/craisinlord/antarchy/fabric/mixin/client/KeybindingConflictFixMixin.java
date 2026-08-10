package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.fabric.client.KeybindingConflictFixStore;
import com.mojang.blaze3d.platform.InputConstants;
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
    @Shadow @Final private static Map<InputConstants.Key, KeyMapping> MAP;
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow private InputConstants.Key key;
    @Shadow private boolean isDown;

    @Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("TAIL"))
    private void antarchy$addInitialMapping(String name, InputConstants.Type type, int key, String category, CallbackInfo ci) {
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
        KeyMapping active = MAP.get(key);
        for (KeyMapping mapping : KeybindingConflictFixStore.others(key, active)) {
            ((KeybindingConflictFixMixin) (Object) mapping).antarchy$setDown(down);
        }
    }

    @Unique
    private void antarchy$setDown(boolean down) {
        this.isDown = down;
    }
}
