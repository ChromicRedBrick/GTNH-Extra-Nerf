package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.enums.VoltageIndex;
import gregtech.common.tileentities.generators.MTEGasTurbine;
import pl.kuba6000.gtnhextranerf.mixins.support.GeneratorEfficiency;

@Mixin(value = MTEGasTurbine.class, remap = false)
public abstract class MixinMTEGasTurbine {

    @ModifyReturnValue(method = "getEfficiency()I", at = @At("RETURN"), require = 1)
    private int gtnhextranerf$generatorEfficiency(int original) {
        return GeneratorEfficiency.forTier(((MTEGasTurbine) (Object) this).mTier, VoltageIndex.LV);
    }
}
