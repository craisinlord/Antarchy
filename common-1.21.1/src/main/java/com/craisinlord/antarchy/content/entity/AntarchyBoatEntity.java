package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AntarchyBoatEntity extends Boat {
    private final String dropItemPath;

    public AntarchyBoatEntity(EntityType<? extends Boat> entityType, Level level, String dropItemPath) {
        super(entityType, level);
        this.dropItemPath = dropItemPath;
        this.setVariant(Type.OAK);
    }

    @Override
    public Item getDropItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, this.dropItemPath));
    }
}
