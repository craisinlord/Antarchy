package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class HordeClientState {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static final int STALE_SYNC_TICKS = 20 * 15;
    private static float targetIntensity;
    private static float intensity;
    private static int heartbeatDelayTicks;
    private static int ticksSinceSync;

    private HordeClientState() {
    }

    public static void update(float syncedIntensity) {
        targetIntensity = Mth.clamp(syncedIntensity, 0.0F, 1.0F);
        ticksSinceSync = 0;
    }

    public static float intensity() {
        return intensity;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || !minecraft.level.dimension().equals(CAVARYN) || !minecraft.player.isAlive()) {
            reset();
        }

        intensity += (targetIntensity - intensity) * 0.08F;
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        if (minecraft.level.dimension().equals(CAVARYN)) {
            if (++ticksSinceSync >= STALE_SYNC_TICKS) {
                targetIntensity = 0.0F;
            }
            tickHeartbeat(minecraft);
        }
        if (intensity < 0.01F) {
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

    private static void reset() {
        targetIntensity = 0.0F;
        intensity = 0.0F;
        heartbeatDelayTicks = 0;
        ticksSinceSync = 0;
    }

    private static void tickHeartbeat(Minecraft minecraft) {
        if (heartbeatDelayTicks > 0) {
            heartbeatDelayTicks--;
            return;
        }

        float soundIntensity = Mth.clamp(intensity, 0.0F, 1.0F);
        float scaledIntensity = soundIntensity * soundIntensity;
        float volume = 0.08F + scaledIntensity * 1.42F;
        float pitch = 0.85F + soundIntensity * 0.45F;
        heartbeatDelayTicks = Mth.clamp((int) (150.0F - scaledIntensity * 132.0F), 18, 150);
        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                AntarchySoundEvents.CAVARYN_HEARTBEAT.get(),
                SoundSource.AMBIENT,
                volume,
                pitch,
                false
        );
    }
}
