package com.craisinlord.antarchy.content.network;

import com.craisinlord.antarchy.Antarchy;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DiamondMinecartInputPayload(byte inputFlags) implements CustomPacketPayload {
    public static final Type<DiamondMinecartInputPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "diamond_minecart_input"));

    public static final StreamCodec<ByteBuf, DiamondMinecartInputPayload> STREAM_CODEC =
            ByteBufCodecs.BYTE.map(DiamondMinecartInputPayload::new, DiamondMinecartInputPayload::inputFlags);

    public static final byte FLAG_FORWARD = 0x01;
    public static final byte FLAG_BACK    = 0x02;
    public static final byte FLAG_LEFT    = 0x04;
    public static final byte FLAG_RIGHT   = 0x08;

    public boolean isForward() { return (inputFlags & FLAG_FORWARD) != 0; }
    public boolean isBack()    { return (inputFlags & FLAG_BACK)    != 0; }
    public boolean isLeft()    { return (inputFlags & FLAG_LEFT)    != 0; }
    public boolean isRight()   { return (inputFlags & FLAG_RIGHT)   != 0; }

    @Override
    public Type<DiamondMinecartInputPayload> type() {
        return TYPE;
    }
}
