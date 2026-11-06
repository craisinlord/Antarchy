package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

public class HoverboardModel extends GeoModel<HoverboardEntity> {
    public static final DataTicket<String> COLOR = DataTicket.create("antarchy_hoverboard_color", String.class);

    private static final String UNDYED = "";

    private static final Identifier MODEL = Identifier.fromNamespaceAndPath(Antarchy.MODID, "hoverboard");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath(Antarchy.MODID, "hoverboard");
    private static final Identifier BASE_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hoverboard/hoverboard.png");

    @Override
    public void addAdditionalStateData(HoverboardEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        DyeColor color = animatable.getColor();
        renderState.addGeckolibData(COLOR, color == null ? UNDYED : color.getName());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String color = renderState.getOrDefaultGeckolibData(COLOR, UNDYED);
        if (color.isEmpty()) {
            return BASE_TEXTURE;
        }
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/hoverboard/hoverboard_" + color + ".png");
    }

    @Override
    public Identifier getAnimationResource(HoverboardEntity animatable) {
        return ANIMATION;
    }
}
