package com.craisinlord.antarchy.content.worldgen.elythia;

import org.jetbrains.annotations.Nullable;

/**
 * Implemented on {@code RandomState}. Holds the wired {@link ElythiaHydrology} from the noise router's vein_toggle
 * slot, captured before other mods (C2ME's density function compiler) replace the router with compiled functions.
 */
public interface ElythiaHydrologyHolder {
    @Nullable
    ElythiaHydrology antarchy$hydrology();
}
