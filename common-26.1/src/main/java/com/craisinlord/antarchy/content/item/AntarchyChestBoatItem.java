package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.AntarchyChestBoatEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AntarchyChestBoatItem extends OuranwoodBoatItem {
    private final EntityType<AntarchyChestBoatEntity> entityType;
    private final String dropItemPath;

    public AntarchyChestBoatItem(EntityType<AntarchyChestBoatEntity> entityType, String dropItemPath, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.dropItemPath = dropItemPath;
    }

    @Override
    protected AbstractBoat createBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 pos = hitResult.getLocation();
        AntarchyChestBoatEntity boat = new AntarchyChestBoatEntity(this.entityType, level, this.dropItemPath);
        boat.setPos(pos.x, pos.y, pos.z);
        boat.xo = pos.x;
        boat.yo = pos.y;
        boat.zo = pos.z;
        this.applyDefaultStackConfig(level, stack, player, boat);
        return boat;
    }
}
