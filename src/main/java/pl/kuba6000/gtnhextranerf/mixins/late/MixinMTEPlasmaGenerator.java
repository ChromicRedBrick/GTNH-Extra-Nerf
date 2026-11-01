package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.enums.VoltageIndex;
import gregtech.common.tileentities.generators.MTEPlasmaGenerator;
import pl.kuba6000.gtnhextranerf.mixins.support.GeneratorEfficiency;

@Mixin(value = MTEPlasmaGenerator.class, remap = false)
public abstract class MixinMTEPlasmaGenerator {

    @ModifyReturnValue(method = "getEfficiency()I", at = @At("RETURN"), require = 1)
    private int gtnhextranerf$generatorEfficiency(int original) {
        return GeneratorEfficiency.forTier(((MTEPlasmaGenerator) (Object) this).mTier, VoltageIndex.EV);
    }
}
