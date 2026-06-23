package com.craisinlord.antarchy.neoforge.mixins.infinity;

import com.craisinlord.antarchy.Antarchy;
import net.lerariemann.infinity.util.config.ConfigGenInvocation;
import net.lerariemann.infinity.util.config.ConfigGenerator;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ConfigGenInvocation.class, remap = false)
/*
 * Guards against a NPE in Infinite Dimensions' SurfaceRuleScanner when another mod
 * registers a surface rule with a missing "biome_is" field. Without this, the crash
 * propagates up through PlayerList.placeNewPlayer and kicks the player on login
 */
public class ConfigGenInvocationMixin {

    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/lerariemann/infinity/util/config/ConfigGenerator;generateAll(Lnet/minecraft/server/MinecraftServer;)V"
            ),
            remap = false
    )
    private void antarchy$safeGenerateAll(MinecraftServer server) {
        try {
            ConfigGenerator.generateAll(server);
        } catch (Exception e) {
            Antarchy.LOGGER.error(
                    "[Antarchy] Infinite Dimensions surface rule scan crashed — likely a mod has a malformed surface rule (null 'biome_is' entry). Player login will continue.",
                    e
            );
        }
    }
}
