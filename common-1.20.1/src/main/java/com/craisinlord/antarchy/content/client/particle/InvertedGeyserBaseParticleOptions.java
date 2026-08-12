package com.craisinlord.antarchy.content.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public record InvertedGeyserBaseParticleOptions(
        ParticleType<InvertedGeyserBaseParticleOptions> type,
        int fluidBlocks,
        float burstImpulseBase,
        Direction direction
) implements ParticleOptions {

    public static ParticleOptions.Deserializer<InvertedGeyserBaseParticleOptions> deserializer() {
        return new ParticleOptions.Deserializer<>() {
            @Override
            public InvertedGeyserBaseParticleOptions fromCommand(ParticleType<InvertedGeyserBaseParticleOptions> type, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                int fluidBlocks = reader.readInt();
                reader.expect(' ');
                float burstImpulseBase = reader.readFloat();
                reader.expect(' ');
                Direction direction = "down".equals(reader.readUnquotedString()) ? Direction.DOWN : Direction.UP;
                return new InvertedGeyserBaseParticleOptions(type, fluidBlocks, burstImpulseBase, direction);
            }

            @Override
            public InvertedGeyserBaseParticleOptions fromNetwork(ParticleType<InvertedGeyserBaseParticleOptions> type, FriendlyByteBuf buffer) {
                int fluidBlocks = buffer.readVarInt();
                float burstImpulseBase = buffer.readFloat();
                Direction direction = buffer.readBoolean() ? Direction.DOWN : Direction.UP;
                return new InvertedGeyserBaseParticleOptions(type, fluidBlocks, burstImpulseBase, direction);
            }
        };
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.fluidBlocks);
        buffer.writeFloat(this.burstImpulseBase);
        buffer.writeBoolean(this.direction == Direction.DOWN);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%s %d %f %s",
                BuiltInRegistries.PARTICLE_TYPE.getKey(this.type),
                this.fluidBlocks,
                this.burstImpulseBase,
                this.direction == Direction.DOWN ? "down" : "up");
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
