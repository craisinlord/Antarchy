package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

public class TruffaloChestBoatEntity extends ChestBoat {
    public TruffaloChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level, () -> BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "truffalo_chest_boat")));
    }
}
