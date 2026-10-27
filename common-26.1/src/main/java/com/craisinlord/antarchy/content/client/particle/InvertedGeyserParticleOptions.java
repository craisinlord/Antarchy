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

public record InvertedGeyserParticleOptions(
        ParticleType<InvertedGeyserParticleOptions> type,
        int fluidBlocks,
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

    public static MapCodec<InvertedGeyserParticleOptions> codec(ParticleType<InvertedGeyserParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.fieldOf("fluid_blocks").forGetter(InvertedGeyserParticleOptions::fluidBlocks),
                VERTICAL_DIRECTION_CODEC.fieldOf("direction").forGetter(InvertedGeyserParticleOptions::direction)
        ).apply(instance, (fluidBlocks, direction) -> new InvertedGeyserParticleOptions(type, fluidBlocks, direction)));
    }

    public static StreamCodec<? super ByteBuf, InvertedGeyserParticleOptions> streamCodec(ParticleType<InvertedGeyserParticleOptions> type) {
        return new StreamCodec<>() {
            @Override
            public InvertedGeyserParticleOptions decode(ByteBuf buffer) {
                int fluidBlocks = ByteBufCodecs.INT.decode(buffer);
                Direction direction = VERTICAL_DIRECTION_STREAM_CODEC.decode(buffer);
                return new InvertedGeyserParticleOptions(type, fluidBlocks, direction);
            }

            @Override
            public void encode(ByteBuf buffer, InvertedGeyserParticleOptions value) {
                ByteBufCodecs.INT.encode(buffer, value.fluidBlocks());
                VERTICAL_DIRECTION_STREAM_CODEC.encode(buffer, value.direction());
            }
        };
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
