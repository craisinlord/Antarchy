package com.craisinlord.antarchy.content.portal;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.ant.BrownAntEntity;
import com.craisinlord.antarchy.content.entity.ant.RedAntEntity;
import com.craisinlord.antarchy.content.entity.ant.TermiteEntity;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public enum PermanentPortalType {
    ELYTHIA("elythia", AntarchySettings::brownAntDestinationDimension, AntarchySettings::elythiaPortalEnabled, AntarchyTags.Blocks.ELYTHIA_PORTAL_FRAMES) {
        @Override
        boolean matchesSacrifice(LivingEntity entity) {
            return entity instanceof BrownAntEntity;
        }
    },
    THORAXIS("thoraxis", AntarchySettings::redAntDestinationDimension, AntarchySettings::thoraxisPortalEnabled, AntarchyTags.Blocks.THORAXIS_PORTAL_FRAMES) {
        @Override
        boolean matchesSacrifice(LivingEntity entity) {
            return entity instanceof RedAntEntity;
        }
    },
    CAVARYN("cavaryn", AntarchySettings::termiteDestinationDimension, AntarchySettings::cavarynPortalEnabled, AntarchyTags.Blocks.CAVARYN_PORTAL_FRAMES) {
        @Override
        boolean matchesSacrifice(LivingEntity entity) {
            return entity instanceof TermiteEntity;
        }
    };

    private final String id;
    private final Supplier<ResourceKey<Level>> primaryDimensionSupplier;
    private final Supplier<Boolean> enabledSupplier;
    private final TagKey<Block> frameTag;
    private Supplier<Block> platformBlockSupplier;
    private Supplier<Block> portalBlockSupplier;

    PermanentPortalType(String id, Supplier<ResourceKey<Level>> primaryDimensionSupplier, Supplier<Boolean> enabledSupplier, TagKey<Block> frameTag) {
        this.id = id;
        this.primaryDimensionSupplier = primaryDimensionSupplier;
        this.enabledSupplier = enabledSupplier;
        this.frameTag = frameTag;
    }

    public static void bindBlocks(
            Supplier<Block> elythiaPlatform,
            Supplier<Block> elythiaPortal,
            Supplier<Block> thoraxisPlatform,
            Supplier<Block> thoraxisPortal,
            Supplier<Block> cavarynPlatform,
            Supplier<Block> cavarynPortal
    ) {
        ELYTHIA.bind(elythiaPlatform, elythiaPortal);
        THORAXIS.bind(thoraxisPlatform, thoraxisPortal);
        CAVARYN.bind(cavarynPlatform, cavarynPortal);
    }

    public static boolean portalsEnabled() {
        return AntarchySettings.permanentPortalsEnabled();
    }

    @Nullable
    public static PermanentPortalType fromSacrifice(LivingEntity entity) {
        if (!portalsEnabled()) {
            return null;
        }
        for (PermanentPortalType type : values()) {
            if (type.isEnabled() && type.matchesSacrifice(entity)) {
                return type;
            }
        }
        return null;
    }

    private void bind(Supplier<Block> platformBlockSupplier, Supplier<Block> portalBlockSupplier) {
        this.platformBlockSupplier = Objects.requireNonNull(platformBlockSupplier);
        this.portalBlockSupplier = Objects.requireNonNull(portalBlockSupplier);
    }

    public String id() {
        return this.id;
    }

    public boolean isEnabled() {
        return portalsEnabled() && this.enabledSupplier.get();
    }

    public ResourceKey<Level> primaryDimension() {
        return this.primaryDimensionSupplier.get();
    }

    public TagKey<Block> frameTag() {
        return this.frameTag;
    }

    public Block platformBlock() {
        return Objects.requireNonNull(this.platformBlockSupplier, "Platform block supplier not bound for " + this.id).get();
    }

    public Block portalBlock() {
        return Objects.requireNonNull(this.portalBlockSupplier, "Portal block supplier not bound for " + this.id).get();
    }

    abstract boolean matchesSacrifice(LivingEntity entity);
}
