package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.enums.VoltageIndex;
import gregtech.common.tileentities.generators.MTEDieselGenerator;
import pl.kuba6000.gtnhextranerf.mixins.support.GeneratorEfficiency;

@Mixin(value = MTEDieselGenerator.class, remap = false)
public abstract class MixinMTEDieselGenerator {

    @ModifyReturnValue(method = "getEfficiency()I", at = @At("RETURN"), require = 1)
    private int gtnhextranerf$generatorEfficiency(int original) {
        return GeneratorEfficiency.forTier(((MTEDieselGenerator) (Object) this).mTier, VoltageIndex.LV);
    }
}
