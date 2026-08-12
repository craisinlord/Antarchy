package com.craisinlord.antarchy.content.minecart;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface AntimetalMinecartAccess {
    boolean antarchy$isOnAntimetalRail();

    void antarchy$setOnAntimetalRail(boolean value);

    @Nullable
    BlockPos antarchy$getAntimetalRailPos();

    void antarchy$setAntimetalRailPos(@Nullable BlockPos pos);
}
