package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import com.craisinlord.antarchy.compat.network.ByteBufCodecs;
import com.craisinlord.antarchy.compat.network.StreamCodec;
import com.craisinlord.antarchy.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimeDilationRatePayload(UUID entityUuid, double rate) implements CustomPacketPayload {
    public static final Type<TimeDilationRatePayload> TYPE =
            new Type<>(new ResourceLocation(Antarchy.MODID, "time_dilation_rate"));
    private static final StreamCodec<ByteBuf, UUID> UUID_CODEC = new StreamCodec<>() {
        @Override
        public UUID decode(ByteBuf buf) {
            return new UUID(buf.readLong(), buf.readLong());
        }

        @Override
        public void encode(ByteBuf buf, UUID uuid) {
                buf.writeLong(uuid.getMostSignificantBits());
                buf.writeLong(uuid.getLeastSignificantBits());
        }
    };
    public static final StreamCodec<ByteBuf, TimeDilationRatePayload> STREAM_CODEC = StreamCodec.composite(
            UUID_CODEC, TimeDilationRatePayload::entityUuid,
            ByteBufCodecs.DOUBLE, TimeDilationRatePayload::rate,
            TimeDilationRatePayload::new
    );

    @Override
    public Type<TimeDilationRatePayload> type() {
        return TYPE;
    }
}
