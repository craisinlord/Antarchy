package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public final class LumenDripParticle extends TextureSheetParticle {
    private static final int LANDED_TICKS = 8;
    private final SpriteSet sprites;
    private boolean landed;
    private int landedAge;

    private LumenDripParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.setSprite(sprites.get(0, 1));
        this.setSize(0.01F, 0.01F);
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.gravity = 0.06F;
        this.lifetime = 40 + this.random.nextInt(20);
        float shade = 0.9F + this.random.nextFloat() * 0.1F;
        this.setColor(0.0F, 0.745F * shade, 0.89F * shade);
        this.setAlpha(0.9F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.landed) {
            float progress = (this.age - this.landedAge) / (float) LANDED_TICKS;
            this.setAlpha(0.9F * Math.max(0.0F, 1.0F - progress));
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;
        if (this.onGround) {
            this.landed = true;
            this.landedAge = this.age;
            this.lifetime = this.age + LANDED_TICKS;
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            this.setSprite(this.sprites.get(1, 1));
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
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
            return new LumenDripParticle(level, x, y, z, this.sprites);
        }
    }
}
