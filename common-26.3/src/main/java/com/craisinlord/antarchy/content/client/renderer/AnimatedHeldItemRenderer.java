package com.craisinlord.antarchy.content.client.renderer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import net.minecraft.world.item.Item;

public class AnimatedHeldItemRenderer<T extends Item & GeoItem & GeoAnimatable> extends GeoItemRenderer<T> {
    public AnimatedHeldItemRenderer(GeoModel<T> model) {
        super(model);
    }
}
