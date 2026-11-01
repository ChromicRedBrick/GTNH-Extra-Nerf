package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.enums.VoltageIndex;
import gregtech.common.tileentities.generators.MTESteamTurbine;
import pl.kuba6000.gtnhextranerf.mixins.support.GeneratorEfficiency;

@Mixin(value = MTESteamTurbine.class, remap = false)
public abstract class MixinMTESteamTurbine {

    @ModifyReturnValue(method = "getEfficiency", at = @At("RETURN"), require = 1)
    private int gtnhextranerf$modifyEfficiency(int original) {
        return 600 / GeneratorEfficiency.forTier(((MTESteamTurbine) (Object) this).mTier, VoltageIndex.LV);
    }
}
