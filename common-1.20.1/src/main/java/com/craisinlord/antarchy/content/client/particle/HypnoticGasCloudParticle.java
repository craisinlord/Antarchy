package com.craisinlord.antarchy.content.client.particle;

import com.craisinlord.antarchy.content.block.entity.PotentNyxiteBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

public final class HypnoticGasCloudParticle extends NoRenderParticle {
    private final boolean flipped;

    public HypnoticGasCloudParticle(ClientLevel level, double x, double y, double z, boolean flipped) {
        super(level, x, y, z);
        this.flipped = flipped;
        this.lifetime = 20;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age % 2 != 0) {
            return;
        }

        BlockPos source = BlockPos.containing(this.x, this.y, this.z);
        Vec3 spawnPoint = PotentNyxiteBlockEntity.pickRandomHypnoticGasSpawnPoint(this.level, source);
        if (PotentNyxiteBlockEntity.canBeReachedByHypnoticGas(this.level, source, spawnPoint)) {
            PotentNyxiteBlockEntity.spawnHypnoticGasParticle(this.level, spawnPoint, this.flipped);
        }
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final boolean flipped;

        public Provider() {
            this(false);
        }

        public Provider(boolean flipped) {
            this.flipped = flipped;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new HypnoticGasCloudParticle(level, x, y, z, this.flipped);
        }
    }
}
