package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.spatial.StorageWorldProvider;
import ggfab.mte.MTEAdvAssLine;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrors;
import gregtech.common.tileentities.machines.multi.MTEAssemblyLine;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

@Mixin(value = { MTEAssemblyLine.class, MTEAdvAssLine.class }, remap = false)
public abstract class MixinAssemblyLineDimension implements ProgressionMachineNerf.SpatialAssemblyHook {

    @Inject(
        method = "checkMachine(Lgregtech/api/interfaces/tileentity/IGregTechTileEntity;Lnet/minecraft/item/ItemStack;Ljava/util/List;)V",
        at = @At("RETURN"),
        require = 1,
        allow = 4)
    private void gtnhextranerf$preferFluixWorld(IGregTechTileEntity tile, ItemStack controller,
        List<StructureError> errors, CallbackInfo callback) {
        if (tile.isServerSide() && errors.isEmpty() && !(tile.getWorld().provider instanceof StorageWorldProvider)) {
            errors.add(StructureErrors.of(ProgressionMachineNerf.SPATIAL_ASSEMBLY_ERROR_KEY));
        }
    }
}
