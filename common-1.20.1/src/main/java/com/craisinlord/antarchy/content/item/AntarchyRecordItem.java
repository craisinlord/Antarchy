package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.RecordItem;

public class AntarchyRecordItem extends RecordItem {
    public static final int COMPARATOR_OUTPUT = 13;
    public static final float UNDERSIDE_SECONDS = 225.0F;
    public static final float THE_QUEEN_SECONDS = 180.0F;
    public static final float VERDANCE_SECONDS = 135.75F;
    public static final float GROTESQUE_SECONDS = 206.86F;

    private final String song;
    private final int lengthInTicks;

    public AntarchyRecordItem(String song, int comparatorOutput, SoundEvent sound, Properties properties, float lengthInSeconds) {
        super(comparatorOutput, sound, properties, Mth.ceil(lengthInSeconds));
        this.song = song;
        this.lengthInTicks = Mth.ceil(lengthInSeconds * 20.0F);
    }

    public static AntarchyRecordItem underside(SoundEvent sound, Properties properties) {
        return new AntarchyRecordItem("underside", COMPARATOR_OUTPUT, sound, properties, UNDERSIDE_SECONDS);
    }

    public static AntarchyRecordItem theQueen(SoundEvent sound, Properties properties) {
        return new AntarchyRecordItem("the_queen", COMPARATOR_OUTPUT, sound, properties, THE_QUEEN_SECONDS);
    }

    public static AntarchyRecordItem verdance(SoundEvent sound, Properties properties) {
        return new AntarchyRecordItem("verdance", COMPARATOR_OUTPUT, sound, properties, VERDANCE_SECONDS);
    }

    public static AntarchyRecordItem grotesque(SoundEvent sound, Properties properties) {
        return new AntarchyRecordItem("grotesque", COMPARATOR_OUTPUT, sound, properties, GROTESQUE_SECONDS);
    }

    @Override
    public MutableComponent getDisplayName() {
        return Component.translatable("jukebox_song." + Antarchy.MODID + "." + this.song);
    }

    @Override
    public int getLengthInTicks() {
        return this.lengthInTicks;
    }
}
