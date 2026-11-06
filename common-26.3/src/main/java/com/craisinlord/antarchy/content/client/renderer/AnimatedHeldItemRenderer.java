package com.craisinlord.antarchy.content.client.renderer;

import net.minecraft.world.item.Item;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;

public class AnimatedHeldItemRenderer<T extends Item & GeoItem & GeoAnimatable> extends GeoItemRenderer<T> {
    public AnimatedHeldItemRenderer(GeoModel<T> model) {
        super(model);
    }
}
