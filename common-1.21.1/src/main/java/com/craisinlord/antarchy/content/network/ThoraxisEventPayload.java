package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.weather.ThoraxisBubble;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ThoraxisEventPayload(int event, int phase, long phaseStart, long phaseEnd, long seed,
                                   List<ThoraxisBubble> bubbles) implements CustomPacketPayload {
    public static final Type<ThoraxisEventPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis_event"));
    public static final StreamCodec<ByteBuf, ThoraxisEventPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.event);
                buf.writeInt(payload.phase);
                buf.writeLong(payload.phaseStart);
                buf.writeLong(payload.phaseEnd);
                buf.writeLong(payload.seed);
                buf.writeInt(payload.bubbles.size());
                for (ThoraxisBubble bubble : payload.bubbles) {
                    ThoraxisBubble.STREAM_CODEC.encode(buf, bubble);
                }
            },
            buf -> {
                int event = buf.readInt();
                int phase = buf.readInt();
                long phaseStart = buf.readLong();
                long phaseEnd = buf.readLong();
                long seed = buf.readLong();
                int count = buf.readInt();
                List<ThoraxisBubble> bubbles = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    bubbles.add(ThoraxisBubble.STREAM_CODEC.decode(buf));
                }
                return new ThoraxisEventPayload(event, phase, phaseStart, phaseEnd, seed, bubbles);
            }
    );

    @Override
    public Type<ThoraxisEventPayload> type() {
        return TYPE;
    }
}
