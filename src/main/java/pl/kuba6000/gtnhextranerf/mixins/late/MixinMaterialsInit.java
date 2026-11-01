package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.enums.Materials;
import gregtech.loaders.materials.MaterialsInit;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.materials.NerfMaterials;

@Mixin(value = MaterialsInit.class, remap = false)
public abstract class MixinMaterialsInit {

    private static final int FUEL_VALUE_DIVISOR = 4;
    private static final String SET_FUEL = "Lgregtech/api/enums/MaterialBuilder;setFuel(Lgregtech/api/enums/MaterialBuilder$FuelType;I)"
        + "Lgregtech/api/enums/MaterialBuilder;";

    @ModifyArg(
        method = "loadTungstenSteel",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/enums/MaterialBuilder;setDefaultLocalName(Ljava/lang/String;)Lgregtech/api/enums/MaterialBuilder;"),
        index = 0,
        require = 1)
    private static String renameFinishedTungstenSteel(String original) {
        return NerfMaterials.FINISHED_TUNGSTEN_STEEL_NAME;
    }

    @Inject(method = "load", at = @At("TAIL"), require = 1)
    private static void registerBrainrotMaterials(CallbackInfo callback) {
        Materials.add(NerfMaterials::register);
    }

    @ModifyArg(method = "loadBenzene", at = @At(value = "INVOKE", target = SET_FUEL), index = 1, require = 1)
    private static int nerfBenzeneFuelValue(int original) {
        return AuditManager.auditResult(AuditScope.REDUCE_BENZENE_FUEL_VALUE_TO_QUARTER, unit -> {
            unit.recipeModified();
            // GT5 diff-only parity, decided 2026-08-03: benzene was disproportionately strong as an early gas fuel.
            // Keep 25% of the upstream value (360 -> 90) in every fuel map derived during material registration.
            return original / FUEL_VALUE_DIVISOR;
        });
    }

    @ModifyArg(method = "loadPhenol", at = @At(value = "INVOKE", target = SET_FUEL), index = 1, require = 1)
    private static int nerfPhenolFuelValue(int original) {
        return AuditManager.auditResult(AuditScope.REDUCE_PHENOL_FUEL_VALUE_TO_QUARTER, unit -> {
            unit.recipeModified();
            // GT5 diff-only parity, decided 2026-08-03: phenol shared the same overpowered early gas-power niche.
            // Keep 25% of the upstream value (288 -> 72) in every fuel map derived during material registration.
            return original / FUEL_VALUE_DIVISOR;
        });
    }
}
