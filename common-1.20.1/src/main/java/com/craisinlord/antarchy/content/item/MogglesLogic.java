package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public final class MogglesLogic {
    private MogglesLogic() {}

    public static boolean isWearingMoggles(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(AntarchyObjects.MOGGLES.get());
    }
}
