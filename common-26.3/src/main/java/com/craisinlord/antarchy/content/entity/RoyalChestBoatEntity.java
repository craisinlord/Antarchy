package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.level.Level;

public class RoyalChestBoatEntity extends ChestBoat {
    private static final Identifier DROP_ITEM_ID = Identifier.fromNamespaceAndPath(Antarchy.MODID, "royal_chest_boat");

    public RoyalChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level, () -> BuiltInRegistries.ITEM.getValue(DROP_ITEM_ID));
    }
}
