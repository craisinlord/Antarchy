package software.bernie.geckolib.animatable.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

/** Compatibility shim for item render-provider hooks used by the 1.21 source. */
public abstract class GeoRenderProvider {
    public abstract BlockEntityWithoutLevelRenderer getGeoItemRenderer();
}
