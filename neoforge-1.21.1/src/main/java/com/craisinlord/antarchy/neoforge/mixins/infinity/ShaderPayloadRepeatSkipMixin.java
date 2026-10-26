package com.craisinlord.antarchy.neoforge.mixins.infinity;

import net.codexarchonic.infinity.registry.payload.s2c.ShaderS2CPayload;
import net.codexarchonic.infinity.util.loading.ShaderLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.ref.WeakReference;

@Mixin(value = ShaderS2CPayload.class, remap = false)
public class ShaderPayloadRepeatSkipMixin {
    @Unique
    private static WeakReference<ClientLevel> antarchy$lastEmptyLevel = new WeakReference<>(null);

    @Redirect(method = "lambda$receive$0", at = @At(value = "INVOKE",
            target = "Lnet/codexarchonic/infinity/util/loading/ShaderLoader;reloadShaders(Lnet/minecraft/client/Minecraft;Lnet/minecraft/nbt/CompoundTag;Z)V"),
            remap = false)
    private void antarchy$skipRepeatedEmptyReload(Minecraft minecraft, CompoundTag shader, boolean iridescence) {
        boolean empty = shader.isEmpty() && !iridescence;
        ClientLevel level = minecraft.level;
        if (empty && level != null && antarchy$lastEmptyLevel.get() == level) {
            return;
        }
        antarchy$lastEmptyLevel = new WeakReference<>(empty ? level : null);
        ShaderLoader.reloadShaders(minecraft, shader, iridescence);
    }
}
