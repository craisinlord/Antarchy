package com.craisinlord.antarchy.content.effect;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.world.entity.LivingEntity;

/**
 * Version-neutral eligibility rules for the Dilated effect.
 *
 * The 1.21 implementation also checks Royal armor. Those items do not exist
 * in the 1.20 source set yet, so the shared 1.20 rule set intentionally
 * retains the entity-tag checks here and can be extended when Royal content
 * is ported.
 */
public final class TimeDilationEligibility {
    private TimeDilationEligibility() {
    }

    public static boolean canApply(LivingEntity entity) {
        return !entity.getType().is(AntarchyTags.Entities.TIME_DILATION_IMMUNE)
                && !entity.getType().is(AntarchyTags.Entities.DILATED_BLACKLIST);
    }
}
