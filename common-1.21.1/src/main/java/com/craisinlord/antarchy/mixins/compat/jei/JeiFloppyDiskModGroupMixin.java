package com.craisinlord.antarchy.mixins.compat.jei;

import com.craisinlord.antos.content.AntOSObjects;
import java.util.List;
import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "mezz.jei.gui.ingredients.ListElementInfo", remap = false)
public abstract class JeiFloppyDiskModGroupMixin {
    @Shadow @Final private List<String> modIds;
    @Shadow @Final private List<String> modNames;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/api/ingredients/IIngredientHelper;getDisplayModId(Ljava/lang/Object;)Ljava/lang/String;"), remap = false)
    private String antarchy$useDiskNamespace(IIngredientHelper<Object> helper, Object ingredient) {
        if (ingredient instanceof ItemStack stack && stack.is(AntOSObjects.FLOPPY_DISK.get())) {
            ResourceLocation diskId = stack.get(AntOSObjects.FLOPPY_DISK_COMPONENT.get());
            if (diskId != null && "antarchy".equals(diskId.getNamespace())) {
                return diskId.getNamespace();
            }
        }
        return helper.getDisplayModId(ingredient);
    }

    @Inject(method = "getModNameForSorting", at = @At("HEAD"), cancellable = true, remap = false)
    private void antarchy$sortDisksWithAntarchy(CallbackInfoReturnable<String> cir) {
        if (modIds.size() == 2 && "antos".equals(modIds.get(0)) && "antarchy".equals(modIds.get(1))) {
            cir.setReturnValue(modNames.get(1));
        }
    }
}
