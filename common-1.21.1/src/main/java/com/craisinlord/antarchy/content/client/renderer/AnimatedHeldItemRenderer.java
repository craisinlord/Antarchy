package com.craisinlord.antarchy.content.client.renderer;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AnimatedHeldItemRenderer<T extends Item & GeoItem & GeoAnimatable> extends GeoItemRenderer<T> {
    public AnimatedHeldItemRenderer(GeoModel<T> model) {
        super(model);
    }
}
