package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class TemporalGlintParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    private TemporalGlintParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                  SpriteSet sprites, float red, float green, float blue) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.setSprite(sprites.get(this.random));
        this.gravity = 0.0F;
        this.friction = 0.9F;
        this.lifetime = 16 + this.random.nextInt(14);
        this.quadSize = 0.1F + this.random.nextFloat() * 0.08F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.setColor(red, green, blue);
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = life < 0.2F ? life / 0.2F : 1.0F - (life - 0.2F) / 0.8F;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static Provider slow(SpriteSet sprites) {
        return new Provider(sprites, 0.75F, 0.25F, 0.27F);
    }

    public static Provider fast(SpriteSet sprites) {
        return new Provider(sprites, 1.0F, 0.58F, 0.4F);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final float red;
        private final float green;
        private final float blue;

        private Provider(SpriteSet sprites, float red, float green, float blue) {
            this.sprites = sprites;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new TemporalGlintParticle(level, x, y, z, xd, yd, zd, this.sprites, this.red, this.green, this.blue);
        }
    }
}
