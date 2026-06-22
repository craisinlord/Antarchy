package com.craisinlord.antarchy.content.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record InvertedGeyserBaseParticleOptions(
        ParticleType<InvertedGeyserBaseParticleOptions> type,
        int fluidBlocks,
        float burstImpulseBase,
        Direction direction
) implements ParticleOptions {
    private static final Codec<Direction> VERTICAL_DIRECTION_CODEC = Codec.STRING.xmap(
            name -> "down".equals(name) ? Direction.DOWN : Direction.UP,
            Direction::getName
    );
    private static final StreamCodec<ByteBuf, Direction> VERTICAL_DIRECTION_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    name -> "down".equals(name) ? Direction.DOWN : Direction.UP,
                    Direction::getName
            );

    public static MapCodec<InvertedGeyserBaseParticleOptions> codec(ParticleType<InvertedGeyserBaseParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("fluid_blocks").forGetter(InvertedGeyserBaseParticleOptions::fluidBlocks),
                Codec.FLOAT.fieldOf("burst_impulse_base").forGetter(InvertedGeyserBaseParticleOptions::burstImpulseBase),
                VERTICAL_DIRECTION_CODEC.fieldOf("direction").forGetter(InvertedGeyserBaseParticleOptions::direction)
        ).apply(instance, (fluidBlocks, burstImpulseBase, direction) ->
                new InvertedGeyserBaseParticleOptions(type, fluidBlocks, burstImpulseBase, direction)));
    }

    public static StreamCodec<? super ByteBuf, InvertedGeyserBaseParticleOptions> streamCodec(ParticleType<InvertedGeyserBaseParticleOptions> type) {
        return new StreamCodec<>() {
            @Override
            public InvertedGeyserBaseParticleOptions decode(ByteBuf buffer) {
                int fluidBlocks = ByteBufCodecs.INT.decode(buffer);
                float burstImpulseBase = ByteBufCodecs.FLOAT.decode(buffer);
                Direction direction = VERTICAL_DIRECTION_STREAM_CODEC.decode(buffer);
                return new InvertedGeyserBaseParticleOptions(type, fluidBlocks, burstImpulseBase, direction);
            }

            @Override
            public void encode(ByteBuf buffer, InvertedGeyserBaseParticleOptions value) {
                ByteBufCodecs.INT.encode(buffer, value.fluidBlocks());
                ByteBufCodecs.FLOAT.encode(buffer, value.burstImpulseBase());
                VERTICAL_DIRECTION_STREAM_CODEC.encode(buffer, value.direction());
            }
        };
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
