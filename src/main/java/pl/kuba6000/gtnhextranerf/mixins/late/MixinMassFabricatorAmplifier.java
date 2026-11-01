package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.common.tileentities.machines.basic.MTEMassfabricator;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

@Mixin(value = MTEMassfabricator.class, remap = false)
public abstract class MixinMassFabricatorAmplifier extends MTEBasicMachine
    implements ProgressionMachineNerf.MassFabricatorHook {

    protected MixinMassFabricatorAmplifier(String name, int tier, String[] description, ITexture[][][] textures) {
        super(name, tier, 8, description, textures, 1, 1);
    }

    @Inject(method = "checkRecipe", at = @At("HEAD"), cancellable = true, require = 1)
    private void gtnhextranerf$requireAmplifier(CallbackInfoReturnable<Integer> callback) {
        FluidStack amplifier = getFillableStack();
        if (amplifier == null || amplifier.amount < MTEMassfabricator.sUUAperUUM
            || !amplifier.isFluidEqual(Materials.UUAmplifier.getFluid(1))) {
            callback.setReturnValue(FOUND_RECIPE_BUT_DID_NOT_MEET_REQUIREMENTS);
        }
    }
}
