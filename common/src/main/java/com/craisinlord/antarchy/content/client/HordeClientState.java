package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HordeClientState {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static float targetIntensity;
    private static float intensity;

    private HordeClientState() {
    }

    public static void update(float syncedIntensity) {
        targetIntensity = Mth.clamp(syncedIntensity, 0.0F, 1.0F);
    }

    public static float intensity() {
        return intensity;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !minecraft.level.dimension().equals(CAVARYN)) {
            targetIntensity = 0.0F;
        }

        intensity += (targetIntensity - intensity) * 0.08F;
        if (intensity < 0.01F) {
            return;
        }
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }

        int count = Math.max(1, (int) (intensity * 5.0F));
        Vec3 center = minecraft.player.position();
        for (int i = 0; i < count; i++) {
            double x = center.x + (minecraft.level.random.nextDouble() - 0.5D) * 18.0D;
            double y = center.y + minecraft.level.random.nextDouble() * 5.0D;
            double z = center.z + (minecraft.level.random.nextDouble() - 0.5D) * 18.0D;
            minecraft.level.addParticle(ParticleTypes.MYCELIUM, x, y, z, 0.0D, 0.015D + intensity * 0.025D, 0.0D);
        }
    }
}
