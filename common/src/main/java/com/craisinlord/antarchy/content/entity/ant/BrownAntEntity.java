package com.craisinlord.antarchy.content.entity.ant;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;

public class BrownAntEntity extends BaseAntEntity implements GeoEntity {
    public BrownAntEntity(EntityType<? extends BaseAntEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ResourceKey<Level> destinationDimension() {
        return AntarchySettings.brownAntDestinationDimension();
    }

    @Override
    protected TagKey<Item> activationItemsTag() {
        return AntarchyTags.Items.BROWN_ANT_ACTIVATION_ITEMS;
    }

    @Override
    protected TagKey<Item> breedingFoodsTag() {
        return AntarchyTags.Items.BROWN_ANT_BREEDING_FOODS;
    }

    @Override
    protected boolean requiresActivationReagent() {
        return AntarchySettings.brownAntRequiresReagent();
    }

    @Override
    protected String activationMessageKey() {
        return "message.antarchy.brown_ant_activated";
    }

    @Override
    protected String needsReagentMessageKey() {
        return "message.antarchy.brown_ant_needs_reagent";
    }

    @Override
    protected double configuredMaxHealth() {
        return AntarchySettings.brownAntHealth();
    }
}
