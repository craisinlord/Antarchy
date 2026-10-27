package com.craisinlord.antarchy.content.client.model;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.AppleCowEntity;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.EnchantedGoldenAppleCow;
import com.craisinlord.antarchy.content.entity.AppleCowEntityVariants.GoldenAppleCow;
import net.minecraft.resources.Identifier;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class AppleCowModel extends GeoModel<AppleCowEntity> {
    public static final DataTicket<Boolean> BABY = DataTicket.create("antarchy_apple_cow_baby", Boolean.class);
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("antarchy_apple_cow_texture", Identifier.class);
    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "apple_cow");
    private static final Identifier BABY_MODEL =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "baby_apple_cow");
    private static final Identifier ANIMATION =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "apple_cow");
    private static final Identifier BABY_ANIMATION =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "baby_apple_cow");
    private static final Identifier APPLE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/apple_cow.png");
    private static final Identifier APPLE_SHEARED_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/apple_cow_sheared.png");
    private static final Identifier BABY_APPLE_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/baby_apple_cow.png");
    private static final Identifier GOLDEN_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/golden_apple_cow.png");
    private static final Identifier GOLDEN_SHEARED_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/golden_apple_cow_sheared.png");
    private static final Identifier BABY_GOLDEN_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/baby_golden_apple_cow.png");
    private static final Identifier ENCHANTED_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/enchanted_golden_apple_cow.png");
    private static final Identifier ENCHANTED_SHEARED_TEXTURE =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/apple_cow/enchanted_golden_apple_cow_sheared.png");

    @Override
    public void addAdditionalStateData(AppleCowEntity animatable, Object relatedObject, GeoRenderState renderState) {
        super.addAdditionalStateData(animatable, relatedObject, renderState);
        renderState.addGeckolibData(BABY, animatable.isBaby());
        renderState.addGeckolibData(TEXTURE, textureFor(animatable));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(BABY, false) ? BABY_MODEL : MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE, APPLE_TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(AppleCowEntity animatable) {
        return animatable.isBaby() ? BABY_ANIMATION : ANIMATION;
    }

    public static Identifier textureFor(AppleCowEntity entity) {
        if (entity.isBaby()) {
            if (entity instanceof EnchantedGoldenAppleCow) {
                return entity.isSheared() ? ENCHANTED_SHEARED_TEXTURE : ENCHANTED_TEXTURE;
            }
            if (entity instanceof GoldenAppleCow) {
                return entity.isSheared() ? GOLDEN_SHEARED_TEXTURE : BABY_GOLDEN_TEXTURE;
            }
            return entity.isSheared() ? APPLE_SHEARED_TEXTURE : BABY_APPLE_TEXTURE;
        }

        if (entity instanceof EnchantedGoldenAppleCow) {
            return entity.isSheared() ? ENCHANTED_SHEARED_TEXTURE : ENCHANTED_TEXTURE;
        }
        if (entity instanceof GoldenAppleCow) {
            return entity.isSheared() ? GOLDEN_SHEARED_TEXTURE : GOLDEN_TEXTURE;
        }
        return entity.isSheared() ? APPLE_SHEARED_TEXTURE : APPLE_TEXTURE;
    }
}
