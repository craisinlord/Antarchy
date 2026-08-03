package com.craisinlord.antarchy.content.client;

import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public final class TigerEyeClientHooks {
    private static Supplier<Component> camouflageKeyText = () -> Component.translatable("key.antarchy.tigers_eye_camouflage");

    private TigerEyeClientHooks() {
    }

    public static void setCamouflageKeyTextSupplier(Supplier<Component> supplier) {
        camouflageKeyText = supplier;
    }

    public static Component camouflageKeyText() {
        return camouflageKeyText.get();
    }
}
