package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class FireflyParticle extends TextureSheetParticle {
    private static final int FADE_TICKS = 8;

    private FireflyParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.setSprite(sprites.get(0, 1));
        this.lifetime = 30 + this.random.nextInt(78);
        this.quadSize = 0.10F + this.random.nextFloat() * 0.04F;
        this.setColor(0.92F, 0.98F, 0.45F);
        this.setAlpha(0.0F);
        this.yd = 0.004 + this.random.nextDouble() * 0.003;
        this.xd = (this.random.nextDouble() - 0.5) * 0.008;
        this.zd = (this.random.nextDouble() - 0.5) * 0.008;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        float bobOffset = Mth.sin(this.age * 0.18F) * 0.006F;
        this.xd += bobOffset;
        this.zd -= bobOffset;

        this.xd *= 0.94;
        this.yd *= 0.98;
        this.zd *= 0.94;

        this.move(this.xd, this.yd, this.zd);

        float alpha;
        if (this.age < FADE_TICKS) {
            alpha = (float) this.age / FADE_TICKS;
        } else if (this.age > this.lifetime - FADE_TICKS) {
            alpha = (float) (this.lifetime - this.age) / FADE_TICKS;
        } else {
            alpha = 1.0F;
        }
        this.setAlpha(alpha * 0.85F);

        this.age++;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new FireflyParticle(level, x, y, z, this.sprites);
        }
    }
}
