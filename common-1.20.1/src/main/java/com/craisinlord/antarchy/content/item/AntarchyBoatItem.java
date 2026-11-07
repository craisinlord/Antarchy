package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.entity.AntarchyBoatEntity;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AntarchyBoatItem extends OuranwoodBoatItem {
    private final Supplier<? extends EntityType<AntarchyBoatEntity>> entityType;
    private final String dropItemPath;

    public AntarchyBoatItem(Supplier<? extends EntityType<AntarchyBoatEntity>> entityType, String dropItemPath, Properties properties) {
        super(properties);
        this.entityType = entityType;
        this.dropItemPath = dropItemPath;
    }

    @Override
    protected Boat createBoat(Level level, HitResult hitResult, ItemStack stack, Player player) {
        Vec3 pos = hitResult.getLocation();
        AntarchyBoatEntity boat = new AntarchyBoatEntity(this.entityType.get(), level, this.dropItemPath);
        boat.setPos(pos.x, pos.y, pos.z);
        boat.xo = pos.x;
        boat.yo = pos.y;
        boat.zo = pos.z;
        this.applyDefaultStackConfig(level, stack, player, boat);
        return boat;
    }
}
