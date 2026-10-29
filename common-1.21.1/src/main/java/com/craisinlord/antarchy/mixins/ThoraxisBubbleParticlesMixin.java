package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.weather.ThoraxisEvents;
import com.craisinlord.anthology.time.TimeDilationFieldEntity;
import com.craisinlord.anthology.time.TimeDilationParticles;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = TimeDilationParticles.class, remap = false)
public abstract class ThoraxisBubbleParticlesMixin {
    @ModifyVariable(method = "spawnFieldBorders", at = @At("HEAD"), argsOnly = true)
    private static List<TimeDilationFieldEntity> antarchy$hideBubbleBorders(List<TimeDilationFieldEntity> fields) {
        boolean hidden = false;
        for (TimeDilationFieldEntity field : fields) {
            if (ThoraxisEvents.isHiddenField(field)) {
                hidden = true;
                break;
            }
        }
        return hidden ? fields.stream().filter(field -> !ThoraxisEvents.isHiddenField(field)).toList() : fields;
    }
}
