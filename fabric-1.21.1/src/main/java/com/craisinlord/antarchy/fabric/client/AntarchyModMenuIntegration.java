package com.craisinlord.antarchy.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Optional Mod Menu integration entrypoint — declared under the "modmenu" key in
 * fabric.mod.json, never referenced anywhere else in Antarchy's own code.
 *
 * <p>This class is only ever loaded/instantiated by Mod Menu itself. Fabric Loader resolves
 * entrypoints lazily: it reads every mod's fabric.mod.json at startup, but a class named under
 * "entrypoints" is not loaded until something explicitly calls
 * {@code FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class)} for
 * that key — and only Mod Menu does that. So if Mod Menu isn't installed, this class is never
 * touched at all, and "modmenu" is declared as a "suggests" (not "depends") dependency in
 * fabric.mod.json precisely so Antarchy loads identically either way.
 *
 * <p>Mod Menu and Cloth Config are independently optional, so the one runtime guard this
 * integration does need is below: if Mod Menu is present but Cloth Config isn't, building the
 * real screen would touch Cloth Config classes that aren't on the classpath.
 */
public final class AntarchyModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            // Mod Menu always shows the config button once a non-null factory is returned, so
            // there's no clean way to hide it conditionally — handing back the screen that was
            // already open is the safest no-op fallback when Cloth Config isn't installed.
            return parent -> parent;
        }
        // Method reference, not a direct call: this does not load AntarchyClothConfigScreenBuilder
        // or touch any Cloth Config class until the player actually clicks the config button,
        // which only happens after the isModLoaded check above has already passed.
        return AntarchyClothConfigScreenBuilder::build;
    }
}
