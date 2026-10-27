package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;

public class NadirBoatEntity extends Boat {
    private static final Identifier DROP_ITEM_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "nadir_boat");

    public NadirBoatEntity(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level, () -> BuiltInRegistries.ITEM.getValue(DROP_ITEM_ID));
    }
}
