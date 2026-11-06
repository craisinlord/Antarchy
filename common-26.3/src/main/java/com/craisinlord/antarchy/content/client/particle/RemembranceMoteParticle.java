package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class RemembranceMoteParticle extends SingleQuadParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    private final SpriteSet sprites;
    private final float swayPhase;

    private RemembranceMoteParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.xd = 0.0D;
        this.yd = 0.012D + this.random.nextDouble() * 0.012D;
        this.zd = 0.0D;
        this.quadSize = 0.07F + this.random.nextFloat() * 0.04F;
        this.lifetime = 40 + this.random.nextInt(30);
        this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.setSprite(this.sprites.get(this.age, this.lifetime));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        this.setSprite(this.sprites.get(this.age, this.lifetime));
        float wave = this.age * 0.15F + this.swayPhase;
        this.xd = Mth.sin(wave) * 0.006F;
        this.zd = Mth.cos(wave * 0.8F) * 0.006F;
        this.alpha = Math.min(1.0F, (this.lifetime - this.age) / 15.0F);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            return new RemembranceMoteParticle(level, x, y, z, this.sprites);
        }
    }
}
