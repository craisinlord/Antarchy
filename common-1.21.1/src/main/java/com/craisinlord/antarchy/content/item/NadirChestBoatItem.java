package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.NadirChestBoatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NadirChestBoatItem extends OuranwoodBoatItem {
    private final EntityType<NadirChestBoatEntity> entityType;

    public NadirChestBoatItem(EntityType<NadirChestBoatEntity> entityType, Properties properties) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    protected Boat createBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 pos = hitResult.getLocation();
        NadirChestBoatEntity boat = new NadirChestBoatEntity(this.entityType, level);
        boat.setPos(pos.x, pos.y, pos.z);
        boat.xo = pos.x;
        boat.yo = pos.y;
        boat.zo = pos.z;
        this.applyDefaultStackConfig(level, stack, player, boat);
        return boat;
    }
}
