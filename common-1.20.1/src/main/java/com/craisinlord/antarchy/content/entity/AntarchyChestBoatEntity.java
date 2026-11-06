package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AntarchyChestBoatEntity extends ChestBoat {
    private final String dropItemPath;

    public AntarchyChestBoatEntity(EntityType<? extends Boat> entityType, Level level, String dropItemPath) {
        super(entityType, level);
        this.dropItemPath = dropItemPath;
        this.setVariant(Type.OAK);
    }

    @Override
    public Item getDropItem() {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(Antarchy.MODID, this.dropItemPath));
    }
}
