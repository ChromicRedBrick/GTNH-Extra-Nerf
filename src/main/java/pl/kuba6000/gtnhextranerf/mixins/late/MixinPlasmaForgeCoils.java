package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.common.tileentities.machines.multi.MTEPlasmaForge;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

@Mixin(value = MTEPlasmaForge.class, remap = false)
public abstract class MixinPlasmaForgeCoils implements ProgressionMachineNerf.PlasmaCoilHook {

    @Inject(method = "checkMachine", at = @At("TAIL"), require = 1)
    private void gtnhextranerf$checkCompletedCoils(IGregTechTileEntity tile, ItemStack controller,
        List<StructureError> errors, CallbackInfo callback) {
        MTEPlasmaForge machine = (MTEPlasmaForge) (Object) this;
        if (errors.isEmpty() && machine.getCoilLevel()
            .getHeat() < HeatingCoilLevel.UXV.getHeat()) {
            errors.add(StructureErrors.of(ProgressionMachineNerf.PLASMA_COIL_ERROR_KEY));
        }
    }
}
