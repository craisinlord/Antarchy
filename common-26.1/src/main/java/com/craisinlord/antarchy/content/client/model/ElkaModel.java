package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ElkaEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class ElkaModel extends GeoModel<ElkaEntity> {
    public static final DataTicket<Boolean> BABY = DataTicket.create("antarchy_elka_baby", Boolean.class);
    private static final Identifier ADULT_GEO = rl("elka");
    private static final Identifier BABY_GEO = rl("elka_baby");
    private static final Identifier ADULT_ANIM = rl("elka");
    private static final Identifier BABY_ANIM = rl("elka_baby");
    private static final Identifier ADULT_TEXTURE = rl("textures/entity/elka.png");
    private static final Identifier BABY_TEXTURE = rl("textures/entity/elka_baby.png");

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void addAdditionalStateData(ElkaEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(BABY, animatable.isBaby());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(BABY, false) ? BABY_GEO : ADULT_GEO;
    }

    @Override
    public Identifier getAnimationResource(ElkaEntity animatable) {
        return animatable.isBaby() ? BABY_ANIM : ADULT_ANIM;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(BABY, false) ? BABY_TEXTURE : ADULT_TEXTURE;
    }
}
