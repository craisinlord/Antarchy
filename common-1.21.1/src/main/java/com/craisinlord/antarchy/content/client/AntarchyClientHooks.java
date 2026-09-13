package com.craisinlord.antarchy.content.client;

import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public final class AntarchyClientHooks {
    private static Consumer<BlockPos> computerOpener = pos -> {
    };

    private AntarchyClientHooks() {
    }

    public static void setComputerOpener(Consumer<BlockPos> opener) {
        computerOpener = opener;
    }

    public static void openComputer(BlockPos pos) {
        computerOpener.accept(pos);
    }
}
