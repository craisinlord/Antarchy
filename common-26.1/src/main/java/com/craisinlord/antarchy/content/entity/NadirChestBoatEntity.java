package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

public class NadirChestBoatEntity extends ChestBoat {
    public NadirChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level, () -> BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "nadir_chest_boat")));
    }
}
