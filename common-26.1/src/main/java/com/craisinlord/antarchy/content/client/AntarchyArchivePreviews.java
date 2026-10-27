package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.entity.nightmare.NightmareEntity;
import com.craisinlord.antos.api.client.archive.ArchiveEntityPreviewRegistry;
import net.minecraft.resources.Identifier;

public final class AntarchyArchivePreviews {
    private AntarchyArchivePreviews() {}

    public static void register() {
        ArchiveEntityPreviewRegistry.register(Identifier.fromNamespaceAndPath("antarchy", "nightmare"), entity -> {
            if (entity instanceof NightmareEntity nightmare) nightmare.setArchivePreviewFlying();
        });
    }
}
