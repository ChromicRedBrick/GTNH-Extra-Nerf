package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;
import pl.kuba6000.gtnhextranerf.progression.EnergyHatchCertification;

/** Close native energy ports, including unsided injection and cached/legacy energy offers. */
@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinEnergyPorts
    implements ProgressionMachineNerf.TransformerPortHook, EnergyHatchCertification.PortHook {

    @Inject(
        method = { "inputEnergyFrom(Lnet/minecraftforge/common/util/ForgeDirection;Z)Z",
            "outputsEnergyTo(Lnet/minecraftforge/common/util/ForgeDirection;Z)Z", "isEnetInput()Z", "isEnetOutput()Z",
            "canAcceptEnergyThisTick()Z" },
        at = @At("HEAD"),
        cancellable = true,
        require = 5,
        allow = 5)
    private void gtnhextranerf$closedPorts(CallbackInfoReturnable<Boolean> callback) {
        if (gtnhextranerf$portsClosed()) callback.setReturnValue(false);
    }

    @Inject(
        method = { "getOfferedEnergy", "demandedEnergyUnits" },
        at = @At("HEAD"),
        cancellable = true,
        require = 2,
        allow = 2)
    private void gtnhextranerf$noEnergyOffer(CallbackInfoReturnable<Double> callback) {
        if (gtnhextranerf$portsClosed()) callback.setReturnValue(0d);
    }

    @Inject(method = "demandsEnergy", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void gtnhextranerf$noLegacyDemand(CallbackInfoReturnable<Integer> callback) {
        if (gtnhextranerf$portsClosed()) callback.setReturnValue(0);
    }

    @Inject(
        method = { "increaseStoredEnergyUnits", "decreaseStoredEnergyUnits", "decreaseStoredEU", "setStoredEU" },
        at = @At("HEAD"),
        cancellable = true,
        require = 4,
        allow = 4)
    private void gtnhextranerf$freezeHatchBuffer(CallbackInfoReturnable<Boolean> callback) {
        if (EnergyHatchCertification.isLocked(((BaseMetaTileEntity) (Object) this).getMetaTileEntity()))
            callback.setReturnValue(false);
    }

    @Unique
    private boolean gtnhextranerf$portsClosed() {
        BaseMetaTileEntity tile = (BaseMetaTileEntity) (Object) this;
        return ProgressionMachineNerf.isDisabledTransformer(tile.getMetaTileEntity())
            || EnergyHatchCertification.isLocked(tile.getMetaTileEntity());
    }
}
