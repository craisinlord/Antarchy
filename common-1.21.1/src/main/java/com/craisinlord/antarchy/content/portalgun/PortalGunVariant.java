package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.resources.ResourceLocation;

public enum PortalGunVariant {
    DEFAULT("", 0x40A0FF, 0xFF961E, "blue", "orange"),
    ATLAS("atlas", 0x53A6D0, 0x6A2BE0, "blue", "purple"),
    P_BODY("p_body", 0xF9D660, 0xC0282A, "yellow", "red"),
    CRAISIN("craisin", 0x6A2BE0, 0xF9D660, "purple", "gold"),
    LAYNCE("laynce", 0xFF961E, 0x1FBF3A, "orange", "green");

    private final String key;
    private final int blueColor;
    private final int orangeColor;
    private final String blueColorName;
    private final String orangeColorName;
    private final ResourceLocation crosshairTexture;
    private final ResourceLocation modelTexture;
    private final ResourceLocation blueVortexTexture;
    private final ResourceLocation orangeVortexTexture;

    PortalGunVariant(String key, int blueColor, int orangeColor, String blueColorName, String orangeColorName) {
        this.key = key;
        this.blueColor = blueColor;
        this.orangeColor = orangeColor;
        this.blueColorName = blueColorName;
        this.orangeColorName = orangeColorName;
        String prefix = key.isEmpty() ? "" : key + "_";
        this.crosshairTexture = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/gui/portal_gun_" + prefix + "crosshair.png");
        this.modelTexture = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/models/item/portal_gun" + (key.isEmpty() ? "" : "_" + key) + ".png");
        this.blueVortexTexture = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/portal_gun/portal_" + prefix + "blue_vortex.png");
        this.orangeVortexTexture = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/portal_gun/portal_" + prefix + "orange_vortex.png");
    }

    public String key() {
        return this.key;
    }

    public int color(PortalGunPortalEntity.PortalSide side) {
        return side == PortalGunPortalEntity.PortalSide.BLUE ? this.blueColor : this.orangeColor;
    }

    public String colorTranslationKey(PortalGunPortalEntity.PortalSide side) {
        return "tooltip.antarchy.portal_gun.color." + (side == PortalGunPortalEntity.PortalSide.BLUE ? this.blueColorName : this.orangeColorName);
    }

    public ResourceLocation crosshairTexture() {
        return this.crosshairTexture;
    }

    public ResourceLocation modelTexture() {
        return this.modelTexture;
    }

    public ResourceLocation vortexTexture(PortalGunPortalEntity.PortalSide side) {
        return side == PortalGunPortalEntity.PortalSide.BLUE ? this.blueVortexTexture : this.orangeVortexTexture;
    }

    public int id() {
        return this.ordinal();
    }

    public static PortalGunVariant byId(int id) {
        PortalGunVariant[] values = values();
        return id >= 0 && id < values.length ? values[id] : DEFAULT;
    }
}
