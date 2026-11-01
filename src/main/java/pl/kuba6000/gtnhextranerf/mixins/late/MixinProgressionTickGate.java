package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;

@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinProgressionTickGate {

    @Unique
    private boolean gtnhextranerf$wasCertificationLocked;

    @WrapOperation(
        method = "doPostTick",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/metatileentity/MetaTileEntity;onPostTick(Lgregtech/api/interfaces/tileentity/IGregTechTileEntity;J)V"),
        require = 1,
        allow = 1)
    private void gtnhextranerf$gate(MetaTileEntity machine, IGregTechTileEntity tile, long tick,
        Operation<Void> original) {
        if (tile.isServerSide() && MachineProgression.isLocked(machine)) {
            gtnhextranerf$wasCertificationLocked = true;
            // Keep any saved in-flight recipe intact, without processing it or consuming more energy/items.
            tile.setActive(false);
            return;
        }
        if (gtnhextranerf$wasCertificationLocked) {
            gtnhextranerf$wasCertificationLocked = false;
            // Recheck waiting inputs immediately instead of waiting for GT's 600-tick polling interval.
            ((BaseMetaTileEntity) (Object) this).markDirty();
        }
        original.call(machine, tile, tick);
    }
}
