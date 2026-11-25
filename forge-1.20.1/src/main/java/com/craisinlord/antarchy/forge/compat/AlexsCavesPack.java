package com.craisinlord.antarchy.forge.compat;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesIntegration;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.resource.PathPackResources;

public final class AlexsCavesPack {
    private static final String STRUCTURE = "com.github.alexmodguy.alexscaves.server.level.structure.AbstractCaveGenerationStructure";

    private AlexsCavesPack() {
    }

    public static void addPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) {
            return;
        }
        IModFileInfo alexsCaves = ModList.get().getModFileById(AlexsCavesIntegration.MOD_ID);
        for (String name : AlexsCavesIntegration.setup(alexsCaves != null, AntarchySettings.alexsCavesBiomes(),
                path -> Files.exists(alexsCaves.getFile().findResource(path.split("/"))), AlexsCavesPack::structureHooksApplied)) {
            String id = Antarchy.MODID + "/" + name;
            Path root = ModList.get().getModFileById(Antarchy.MODID).getFile().findResource("resourcepacks", name);
            Pack pack = Pack.readMetaAndCreate(id, Component.literal("Antarchy: Alex's Caves"), true,
                    packId -> new PathPackResources(packId, true, root), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
            if (pack != null) {
                event.addRepositorySource(consumer -> consumer.accept(pack));
            }
        }
    }

    private static boolean structureHooksApplied() {
        try {
            Method[] methods = Class.forName(STRUCTURE).getDeclaredMethods();
            return Arrays.stream(methods).anyMatch(method -> method.getName().contains("antarchy$variantY"))
                    && Arrays.stream(methods).noneMatch(method -> method.getName().endsWith("ForVoronoi"));
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }
}
