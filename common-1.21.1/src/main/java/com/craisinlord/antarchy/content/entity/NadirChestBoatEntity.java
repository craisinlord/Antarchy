package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class NadirChestBoatEntity extends ChestBoat {
    public NadirChestBoatEntity(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.setVariant(Type.OAK);
    }

    @Override
    public Item getDropItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nadir_chest_boat"));
    }
}
