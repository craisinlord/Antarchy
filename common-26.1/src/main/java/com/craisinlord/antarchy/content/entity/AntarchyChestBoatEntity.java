package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

public class AntarchyChestBoatEntity extends ChestBoat {
    public AntarchyChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level, String dropItemPath) {
        super(entityType, level, () -> BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, dropItemPath)));
    }
}
