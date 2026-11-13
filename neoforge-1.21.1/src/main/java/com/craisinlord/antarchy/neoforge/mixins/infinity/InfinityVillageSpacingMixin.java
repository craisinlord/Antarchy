package com.craisinlord.antarchy.neoforge.mixins.infinity;

import net.codexarchonic.infinity.dimensions.RandomStructureSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RandomStructureSet.class, remap = false)
public abstract class InfinityVillageSpacingMixin {
    private static final int ANTARCHY_MIN_VILLAGE_SPACING = 24;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void antarchy$spreadVillages(CallbackInfo ci) {
        CompoundTag data = ((RandomStructureSet) (Object) this).data;
        if (data == null) {
            return;
        }
        ListTag structures = data.getList("structures", Tag.TAG_COMPOUND);
        if (structures.isEmpty() || !structures.getCompound(0).getString("structure").contains("village")) {
            return;
        }
        CompoundTag placement = data.getCompound("placement");
        if (!placement.contains("spacing")) {
            return;
        }
        int spacing = placement.getInt("spacing");
        if (spacing >= ANTARCHY_MIN_VILLAGE_SPACING) {
            return;
        }
        int separation = placement.getInt("separation");
        int newSpacing = ANTARCHY_MIN_VILLAGE_SPACING + spacing / 2;
        placement.putInt("spacing", newSpacing);
        placement.putInt("separation", Math.max(4, Math.min(separation + (newSpacing - spacing) / 2, newSpacing - 4)));
    }
}
