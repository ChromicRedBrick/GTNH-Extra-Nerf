package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEWirelessEnergy;
import pl.kuba6000.gtnhextranerf.progression.EnergyHatchCertification;
import tectech.thing.metaTileEntity.hatch.MTEHatchWirelessMulti;

/** Reject before debiting the wireless account, including first-tick and manager-driven refills. */
@Mixin(value = { MTEWirelessEnergy.class, MTEHatchWirelessMulti.class }, remap = false)
public abstract class MixinWirelessHatchCertification implements EnergyHatchCertification.WirelessInputHook {

    @Inject(method = "tryFetchingEnergy", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void gtnhextranerf$noUnlicensedWithdrawal(CallbackInfo callback) {
        if (EnergyHatchCertification.isLocked((MetaTileEntity) (Object) this)) callback.cancel();
    }
}
