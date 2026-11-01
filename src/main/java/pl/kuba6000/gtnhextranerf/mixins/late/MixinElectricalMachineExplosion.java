package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import pl.kuba6000.gtnhextranerf.progression.ElectricalSafety;

@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinElectricalMachineExplosion implements ElectricalSafety.MachineExplosionHook {

    @Inject(
        method = "doExplosion",
        at = @At(value = "INVOKE", target = "Lgregtech/api/metatileentity/MetaTileEntity;onExplosion()V"),
        require = 1,
        allow = 1)
    private void gtnhextranerf$machineExploded(long power, CallbackInfo callback) {
        BaseMetaTileEntity tile = (BaseMetaTileEntity) (Object) this;
        ElectricalSafety.record(
            tile.getWorld(),
            ElectricalSafety.Incident.MACHINE_EXPLOSION,
            tile.xCoord,
            tile.yCoord,
            tile.zCoord);
    }
}
