package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import gregtech.api.objects.overclockdescriber.SteamOverclockDescriber;

@Mixin(value = SteamOverclockDescriber.class, remap = false)
public abstract class MixinSteamOverclockDescriber {

    @ModifyConstant(method = "convertEUToSteam(J)J", constant = @Constant(longValue = 2L), require = 1, allow = 1)
    private static long gtnhextranerf$steamPerEu(long original) {
        return 10L;
    }
}
