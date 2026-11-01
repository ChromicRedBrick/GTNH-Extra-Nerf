package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.enums.VoltageIndex;
import gtPlusPlus.xmod.gregtech.common.tileentities.generators.MTESemiFluidGenerator;
import pl.kuba6000.gtnhextranerf.mixins.support.GeneratorEfficiency;

@Mixin(value = MTESemiFluidGenerator.class, remap = false)
public abstract class MixinMTESemiFluidGenerator {

    @ModifyReturnValue(method = "getEfficiency()I", at = @At("RETURN"), require = 1)
    private int gtnhextranerf$generatorEfficiency(int original) {
        return GeneratorEfficiency.forTier(((MTESemiFluidGenerator) (Object) this).mTier, VoltageIndex.LV);
    }
}
