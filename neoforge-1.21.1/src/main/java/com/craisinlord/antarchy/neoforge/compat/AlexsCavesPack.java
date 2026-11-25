package com.craisinlord.antarchy.neoforge.compat;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesIntegration;
import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesStructures;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.nio.file.Files;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforgespi.language.IModFileInfo;

public final class AlexsCavesPack {
    private AlexsCavesPack() {
    }

    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        IModFileInfo alexsCaves = ModList.get().getModFileById(AlexsCavesIntegration.MOD_ID);
        for (String pack : AlexsCavesIntegration.setup(alexsCaves != null, AntarchySettings.alexsCavesBiomes(),
                path -> Files.exists(alexsCaves.getFile().findResource(path.split("/"))), AlexsCavesStructures::hooksApplied)) {
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "resourcepacks/" + pack), PackType.SERVER_DATA,
                    Component.literal("Antarchy: Alex's Caves"), PackSource.BUILT_IN, true, Pack.Position.TOP);
        }
    }
}
