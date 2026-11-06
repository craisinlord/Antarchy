package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.OuranwoodDeerEntity;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public class OuranwoodDeerModel extends GeoModel<OuranwoodDeerEntity> {
    public static final DataTicket<Boolean> BABY = DataTicket.create("antarchy_ouranwood_deer_baby", Boolean.class);
    public static final DataTicket<OuranwoodDeerEntity.Variant> VARIANT = DataTicket.create("antarchy_ouranwood_deer_variant", OuranwoodDeerEntity.Variant.class);
    private static final Identifier ADULT_GEO = rl("ouranwood_deer");
    private static final Identifier BABY_GEO = rl("ouranwood_deer_baby");
    private static final Identifier ADULT_ANIM = rl("ouranwood_deer");
    private static final Identifier BABY_ANIM = rl("ouranwood_deer_baby");
    private static final Identifier BUCK_TEXTURE = rl("textures/entity/ouranwood_deer/deer_buck.png");
    private static final Identifier DOE_TEXTURE = rl("textures/entity/ouranwood_deer/deer_doe.png");
    private static final Identifier STAG_TEXTURE = rl("textures/entity/ouranwood_deer/deer_stag.png");

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, path);
    }

    @Override
    public void addAdditionalStateData(OuranwoodDeerEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(BABY, animatable.isBaby());
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(BABY, false) ? BABY_GEO : ADULT_GEO;
    }

    @Override
    public Identifier getAnimationResource(OuranwoodDeerEntity animatable) {
        return animatable.isBaby() ? BABY_ANIM : ADULT_ANIM;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState.getOrDefaultGeckolibData(BABY, false)) {
            return STAG_TEXTURE;
        }
        return switch (renderState.getOrDefaultGeckolibData(VARIANT, OuranwoodDeerEntity.Variant.BUCK)) {
            case BUCK -> BUCK_TEXTURE;
            case DOE -> DOE_TEXTURE;
        };
    }
}
