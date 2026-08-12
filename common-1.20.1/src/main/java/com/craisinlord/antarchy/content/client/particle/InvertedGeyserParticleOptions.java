package com.craisinlord.antarchy.content.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record InvertedGeyserParticleOptions(
        ParticleType<InvertedGeyserParticleOptions> type,
        int fluidBlocks,
        Direction direction
) implements ParticleOptions {

    public static ParticleOptions.Deserializer<InvertedGeyserParticleOptions> deserializer() {
        return new ParticleOptions.Deserializer<>() {
            @Override
            public InvertedGeyserParticleOptions fromCommand(ParticleType<InvertedGeyserParticleOptions> type, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                int fluidBlocks = reader.readInt();
                reader.expect(' ');
                Direction direction = "down".equals(reader.readUnquotedString()) ? Direction.DOWN : Direction.UP;
                return new InvertedGeyserParticleOptions(type, fluidBlocks, direction);
            }

            @Override
            public InvertedGeyserParticleOptions fromNetwork(ParticleType<InvertedGeyserParticleOptions> type, FriendlyByteBuf buffer) {
                int fluidBlocks = buffer.readVarInt();
                Direction direction = buffer.readBoolean() ? Direction.DOWN : Direction.UP;
                return new InvertedGeyserParticleOptions(type, fluidBlocks, direction);
            }
        };
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.fluidBlocks);
        buffer.writeBoolean(this.direction == Direction.DOWN);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%s %d %s",
                BuiltInRegistries.PARTICLE_TYPE.getKey(this.type),
                this.fluidBlocks,
                this.direction == Direction.DOWN ? "down" : "up");
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
